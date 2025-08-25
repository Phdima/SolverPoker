package com.example.solverpoker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest

@Composable
fun AdBanner() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val adView = remember {
        BannerAdView(context).apply {
            setAdUnitId("R-M-16913722-1")
            setAdSize(BannerAdSize.stickySize(context,screenWidthDp))
        }
    }


    LaunchedEffect(Unit) {
        adView.loadAd(AdRequest.Builder().build())
    }


    DisposableEffect(Unit) {
        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}