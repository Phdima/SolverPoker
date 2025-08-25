package com.example.solverpoker.presentation.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdViewModel(application: Application) : AndroidViewModel(application) {
    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null
    private val _adLoaded = MutableStateFlow(false)


    init {
        initializeAdLoader()
    }

    private fun initializeAdLoader() {
        interstitialAdLoader = InterstitialAdLoader(getApplication()).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    _adLoaded.value = true
                }

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    _adLoaded.value = false
                }
            })
        }
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder("R-M-16913722-2").build()
        interstitialAdLoader?.loadAd(adRequestConfiguration)
    }

    fun showAd(activity: Activity) {
        interstitialAd?.apply {
            setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {}
                override fun onAdFailedToShow(adError: AdError) {
                    cleanup()
                }
                override fun onAdDismissed() {
                    cleanup()
                }
                override fun onAdClicked() {}
                override fun onAdImpression(impressionData: ImpressionData?) {}
            })
            show(activity)
        }
    }

    private fun cleanup() {
        interstitialAd?.setAdEventListener(null)
        interstitialAd = null
        _adLoaded.value = false
        loadInterstitialAd()
    }

    override fun onCleared() {
        interstitialAdLoader?.setAdLoadListener(null)
        interstitialAdLoader = null
        cleanup()
        super.onCleared()
    }
}