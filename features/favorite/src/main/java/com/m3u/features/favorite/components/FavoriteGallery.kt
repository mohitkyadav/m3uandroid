package com.m3u.features.favorite.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.m3u.core.architecture.configuration.LocalConfiguration
import com.m3u.data.database.entity.LiveHolder
import com.m3u.features.favorite.NavigateToLive
import com.m3u.material.ktx.plus
import com.m3u.material.model.LocalSpacing


// TODO: replace with material3-carousel.
@Composable
internal fun FavouriteGallery(
    contentPadding: PaddingValues,
    liveHolder: LiveHolder,
    rowCount: Int,
    navigateToLive: NavigateToLive,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val configuration = LocalConfiguration.current
    val noPictureMode by configuration.noPictureMode
    val lives = liveHolder.lives
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(rowCount),
        verticalItemSpacing = spacing.medium,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        contentPadding = PaddingValues(spacing.medium) + contentPadding,
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = lives,
            key = { it.id },
            contentType = { it.cover.isNullOrEmpty() }
        ) { live ->
            FavoriteItem(
                live = live,
                noPictureMode = noPictureMode,
                onClick = {
                    navigateToLive(live.id)
                },
                onLongClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}