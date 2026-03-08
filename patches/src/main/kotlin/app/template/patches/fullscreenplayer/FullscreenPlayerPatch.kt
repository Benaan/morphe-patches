package app.template.patches.fullscreenplayer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Prevents the player from automatically entering fullscreen mode in landscape.
 *
 * On wide-but-short screens (e.g. 1920x720 car screens), YouTube Music forces
 * fullscreen when the player opens in landscape because the screen height is
 * under 600dp, causing Y() to return false. This patch intercepts that
 * transition and keeps the player in MAXIMIZED_NOW_PLAYING instead.
 *
 * The fix is injected at the top of the state-transition method K(mxn, float):
 *  - If p() is true (engagement panel open), let original code handle it
 *  - If the target state is not MAXIMIZED_NOW_PLAYING, let original code handle it
 *  - If Y() is true (screen is large enough), let original code handle it
 *  - If orientation is not landscape, let original code handle it
 *  - Otherwise: redirect to MAXIMIZED_NOW_PLAYING and return early
 */
val disableFullscreenOnLandscapeStartPatch = bytecodePatch(
    name = "Disable fullscreen player on landscape start",
    description = "Prevents the player from opening in fullscreen when started in landscape mode on wide/short screens such as car displays."
) {
    compatibleWith("com.google.android.apps.youtube.music"("8.40.54"))

    execute {
        PlayerStateTransitionFingerprint.method.addInstructions(
            0,
            """
                # Skip this intercept if the engagement panel is open (p() == true)
                invoke-virtual {p0}, Lcom/google/android/apps/youtube/music/watchpage/mpp/MppWatchWhileLayout;->p()Z
                move-result v0
                if-nez v0, :cond_skip

                # Skip if target state is not MAXIMIZED_NOW_PLAYING
                sget-object v0, Lmxn;->MAXIMIZED_NOW_PLAYING:Lmxn;
                if-ne p1, v0, :cond_skip

                # Skip if Y() is true (screen is considered large enough by the app)
                invoke-virtual {p0}, Lcom/google/android/apps/youtube/music/watchpage/mpp/MppWatchWhileLayout;->Y()Z
                move-result v0
                if-nez v0, :cond_skip

                # Skip if orientation is not landscape (2 = ORIENTATION_LANDSCAPE)
                invoke-virtual {p0}, Lcom/google/android/apps/youtube/music/watchpage/mpp/MppWatchWhileLayout;->getResources()Landroid/content/res/Resources;
                move-result-object v0
                invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;
                move-result-object v0
                iget v0, v0, Landroid/content/res/Configuration;->orientation:I
                const/4 v1, 0x2
                if-ne v0, v1, :cond_skip

                # Landscape + short screen + MAXIMIZED_NOW_PLAYING: stay maximized, don't go fullscreen
                sget-object v0, Lmxn;->MAXIMIZED_NOW_PLAYING:Lmxn;
                invoke-virtual {p0, v0}, Lcom/google/android/apps/youtube/music/watchpage/mpp/MppWatchWhileLayout;->M(Lmxn;)V
                return-void

                :cond_skip
            """
        )
    }
}
