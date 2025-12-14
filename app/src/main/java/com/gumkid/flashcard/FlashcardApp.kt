package com.gumkid.flashcard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlashcardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Firebase initialization is handled automatically by Firebase SDK
    }
}
