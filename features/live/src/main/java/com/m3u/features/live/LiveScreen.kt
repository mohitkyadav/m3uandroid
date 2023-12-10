package com.m3u.features.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m3u.core.unspecified.UBoolean
import com.m3u.core.unspecified.unspecifiable
import com.m3u.core.util.basic.isNotEmpty
import com.m3u.features.live.components.DlnaDevicesBottomSheet
import com.m3u.features.live.components.rememberDeviceHolder
import com.m3u.features.live.components.rememberDeviceWrapper
import com.m3u.features.live.fragments.LiveFragment
import com.m3u.material.components.Background
import com.m3u.material.components.MaskState
import com.m3u.material.components.rememberMaskState
import com.m3u.material.ktx.LifecycleEffect
import com.m3u.ui.LocalHelper
import com.m3u.ui.OnPipModeChanged
import com.m3u.ui.repeatOnLifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.absoluteValue

@Composable
fun LiveRoute(
    init: LiveEvent.Init,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    viewModel: LiveViewModel = hiltViewModel(),
) {
    val helper = LocalHelper.current

    val state: LiveState by viewModel.state.collectAsStateWithLifecycle()
    val playerState: LiveState.PlayerState by viewModel.playerState.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isDevicesVisible by viewModel.isDevicesVisible.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()

    val volume by viewModel.volume.collectAsStateWithLifecycle()
    var brightness by rememberSaveable { mutableFloatStateOf(helper.brightness) }

    val maskState = rememberMaskState()

    var isPipMode by remember { mutableStateOf(false) }

    LifecycleEffect { event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                if (isPipMode) {
                    viewModel.onEvent(LiveEvent.UninstallMedia)
                }
            }

            else -> {}
        }
    }

    helper.repeatOnLifecycle {
        onUserLeaveHint = {
            if (playerState.videoSize.isNotEmpty) {
                maskState.sleep()
                helper.enterPipMode(playerState.videoSize)
            }
        }
        darkMode = true.unspecifiable
        statusBarVisibility = UBoolean.False
        navigationBarVisibility = UBoolean.False
        onPipModeChanged = OnPipModeChanged { info ->
            isPipMode = info.isInPictureInPictureMode
            if (!isPipMode) {
                maskState.wake()
            }
        }
    }

    LaunchedEffect(init) {
        viewModel.onEvent(init)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { brightness }
            .onEach { helper.brightness = it }
            .launchIn(this)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { maskState.visible }
            .onEach { visible ->
                helper.statusBarVisibility = visible.unspecifiable
                helper.navigationBarVisibility = UBoolean.False
            }
            .launchIn(this)
    }

    DisposableEffect(Unit) {
        val prev = helper.brightness
        onDispose {
            helper.brightness = prev
        }
    }

    Background(
        color = Color.Black,
        contentColor = Color.White
    ) {
        // TODO: replace with material3-carousel.
        DlnaDevicesBottomSheet(
            maskState = maskState,
            searching = searching,
            isDevicesVisible = isDevicesVisible,
            deviceHolder = rememberDeviceHolder(devices),
            connected = rememberDeviceWrapper(state.connected),
            connectDlnaDevice = { viewModel.onEvent(LiveEvent.ConnectDlnaDevice(it)) },
            disconnectDlnaDevice = { viewModel.onEvent(LiveEvent.DisconnectDlnaDevice(it)) },
            onDismiss = { viewModel.onEvent(LiveEvent.CloseDlnaDevices) }
        )

        LiveScreen(
            init = state.init,
            recording = state.recording,
            openDlnaDevices = { viewModel.onEvent(LiveEvent.OpenDlnaDevices) },
            onRecord = { viewModel.onEvent(LiveEvent.Record) },
            onFavourite = { viewModel.onEvent(LiveEvent.OnFavourite(it)) },
            onBackPressed = onBackPressed,
            maskState = maskState,
            playerState = playerState,
            onInstallMedia = { viewModel.onEvent(LiveEvent.InstallMedia(it)) },
            onUninstallMedia = { viewModel.onEvent(LiveEvent.UninstallMedia) },
            brightness = brightness,
            volume = volume,
            onBrightness = { brightness = it },
            onVolume = { viewModel.onEvent(LiveEvent.OnVolume(it)) },
            modifier = modifier
        )
    }
}

@Composable
private fun LiveScreen(
    init: LiveState.Init,
    recording: Boolean,
    openDlnaDevices: () -> Unit,
    onRecord: () -> Unit,
    onFavourite: (String) -> Unit,
    onBackPressed: () -> Unit,
    maskState: MaskState,
    playerState: LiveState.PlayerState,
    onInstallMedia: (String) -> Unit,
    onUninstallMedia: () -> Unit,
    volume: Float,
    brightness: Float,
    onVolume: (Float) -> Unit,
    onBrightness: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = MaterialTheme.colorScheme
    when (init) {
        is LiveState.InitOne -> {
            val live = init.live
            val url = live?.url.orEmpty()
            val favourite = live?.favourite ?: false
            LiveFragment(
                playerState = playerState,
                title = init.live?.title ?: "--",
                url = url,
                cover = init.live?.cover.orEmpty(),
                feedTitle = init.feed?.title ?: "--",
                maskState = maskState,
                recording = recording,
                stared = favourite,
                onRecord = onRecord,
                onFavourite = { onFavourite(url) },
                openDlnaDevices = openDlnaDevices,
                onBackPressed = onBackPressed,
                onInstallMedia = onInstallMedia,
                onUninstallMedia = onUninstallMedia,
                brightness = brightness,
                volume = volume,
                onBrightness = onBrightness,
                onVolume = onVolume,
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .testTag("features:live")
            )
        }

        is LiveState.InitPlayList -> {
            // TODO: move pager into mask
            val pagerState = rememberPagerState(init.initialIndex) { init.lives.size }
            VerticalPager(
                state = pagerState,
                modifier = modifier
                    .fillMaxSize()
                    .background(theme.background)
            ) { pageIndex ->
                val live = init.lives[pageIndex]
                val url = live.url
                val favourite = live.favourite
                LiveFragment(
                    playerState = playerState,
                    title = live.title,
                    feedTitle = init.feed?.title.orEmpty(),
                    url = url,
                    cover = live.cover.orEmpty(),
                    maskState = maskState,
                    recording = recording,
                    stared = favourite,
                    onRecord = onRecord,
                    onFavourite = { onFavourite(url) },
                    openDlnaDevices = openDlnaDevices,
                    onBackPressed = onBackPressed,
                    onInstallMedia = onInstallMedia,
                    onUninstallMedia = onUninstallMedia,
                    volume = volume,
                    brightness = brightness,
                    onVolume = onVolume,
                    onBrightness = onBrightness,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .scrollableGroup(
                            current = pagerState.currentPage - pageIndex,
                            offsetFraction = pagerState.currentPageOffsetFraction
                        )
                )
            }
        }
    }
}

private fun Modifier.scrollableGroup(
    current: Int,
    offsetFraction: Float
): Modifier = graphicsLayer {
    val offset = current + offsetFraction
        .absoluteValue
        .coerceIn(0f, 1f)
    val scale = lerp(1f, 0.8f, offset)
    scaleX = scale
    scaleY = scale
}

