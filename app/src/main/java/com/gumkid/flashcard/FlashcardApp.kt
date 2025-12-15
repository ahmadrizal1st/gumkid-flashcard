package com.gumkid.flashcard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.FirebaseApp
import com.gumkid.flashcard.util.NotificationHelper

@HiltAndroidApp
class FlashcardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}
