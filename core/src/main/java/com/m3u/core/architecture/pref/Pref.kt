package com.m3u.core.architecture.pref

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import com.m3u.core.annotation.ClipMode
import com.m3u.core.annotation.ConnectTimeout
import com.m3u.core.annotation.FeedStrategy
import kotlinx.coroutines.flow.Flow

@Stable
interface Pref {
    @FeedStrategy
    var feedStrategy: Int
    var useCommonUIMode: Boolean
    var rowCount: Int

    @ConnectTimeout
    var connectTimeout: Long
    var godMode: Boolean
    var experimentalMode: Boolean

    @ClipMode
    var clipMode: Int
    var autoRefresh: Boolean
    var fullInfoPlayer: Boolean

    @ExperimentalPref
    var scrollMode: Boolean

    @ExperimentalPref
    var isSSLVerification: Boolean
    var rootDestination: Int
    var noPictureMode: Boolean

    @ExperimentalPref
    var cinemaMode: Boolean
    var useDynamicColors: Boolean

    var zapMode: Boolean

    companion object {
        @FeedStrategy
        const val DEFAULT_FEED_STRATEGY = FeedStrategy.SKIP_FAVORITE
        const val DEFAULT_USE_COMMON_UI_MODE = false
        const val DEFAULT_ROW_COUNT = 1

        @ConnectTimeout
        const val DEFAULT_CONNECT_TIMEOUT = ConnectTimeout.SHORT
        const val DEFAULT_GOD_MODE = false
        const val DEFAULT_EXPERIMENTAL_MODE = false

        @ClipMode
        const val DEFAULT_CLIP_MODE = ClipMode.ADAPTIVE
        const val DEFAULT_SCROLL_MODE = false
        const val DEFAULT_AUTO_REFRESH = false
        const val DEFAULT_SSL_VERIFICATION = false
        const val DEFAULT_FULL_INFO_PLAYER = false
        const val DEFAULT_INITIAL_ROOT_DESTINATION = 0
        const val DEFAULT_NO_PICTURE_MODE = true
        const val DEFAULT_CINEMA_MODE = false

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        var DEFAULT_USE_DYNAMIC_COLORS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        const val DEFAULT_ZAP_MODE = false

        const val FEED_STRATEGY = "feedStrategy"
        const val USE_COMMON_UI_MODE = "useCommonUIMode"
        const val ROW_COUNT = "rowCount"

        const val CONNECT_TIMEOUT = "connectTimeout"
        const val GOD_MODE = "godMode"
        const val EXPERIMENTAL_MODE = "experimentalMode"

        const val CLIP_MODE = "clipMode"
        const val SCROLL_MODE = "scrollMode"
        const val AUTO_REFRESH = "autoRefresh"
        const val SSL_VERIFICATION = "sslVerification"
        const val FULL_INFO_PLAYER = "fullInfoPlayer"
        const val INITIAL_ROOT_DESTINATION = "initialRootDestination"
        const val NO_PICTURE_MODE = "noPictureMode"
        const val CINEMA_MODE = "cinemaMode"
        const val USE_DYNAMIC_COLORS = "use-dynamic-colors"
        const val ZAP_MODE = "zap-mode"
    }
}

fun Pref.observeAsFlow(): Flow<Pref> = when (this) {
    is SharedPref -> snapshotFlow { this }
    else -> error("Pref.observeAsFlow only be allowed when it is SharedPref.")
}
