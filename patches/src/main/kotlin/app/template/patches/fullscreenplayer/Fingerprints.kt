package app.template.patches.fullscreenplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.literal
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Fingerprint for MppWatchWhileLayout.K(mxn, float).
 *
 * This method handles player state transitions during fling/swipe gestures.
 * In landscape mode when the screen height < 600dp (e.g. car screens), it forces
 * the player into FULLSCREEN state even on non-tablet devices.
 *
 * Identified by:
 *  - Non-obfuscated class MppWatchWhileLayout
 *  - Parameters: (mxn state, float progress)
 *  - Contains a call to Resources.getConfiguration() to check orientation
 *  - Contains orientation literal 2 (ORIENTATION_LANDSCAPE)
 */
object PlayerStateTransitionFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("L", "F"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/res/Resources;",
            name = "getConfiguration",
        ),
        literal(2), // Configuration.ORIENTATION_LANDSCAPE
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/android/apps/youtube/music/watchpage/mpp/MppWatchWhileLayout;"
    }
)
