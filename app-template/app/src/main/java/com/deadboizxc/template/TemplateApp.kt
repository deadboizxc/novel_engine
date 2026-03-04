package com.deadboizxc.template

import android.app.Application

/**
 * Application class - точка входа приложения
 */
class TemplateApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Инициализация DI, логгеров, analytics и т.д.
    }
}
