package com.boom.ems.emsboom;

import android.app.Application
import io.realm.Realm;
import io.realm.RealmConfiguration



class EMSApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        val config = RealmConfiguration.Builder()
                .name("whitelist.realm").build()
        Realm.setDefaultConfiguration(config)
    }
}