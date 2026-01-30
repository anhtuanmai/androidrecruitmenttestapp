package fr.leboncoin.core.network.di

import javax.inject.Qualifier

/**
 * Qualifier for ImageLoader with persistent disk cache (7-day expiration).
 * Use for images that should be cached across app launches.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PersistentImageLoader

/**
 * Qualifier for ImageLoader with session-only memory cache.
 * Cache is cleared when app process dies. No disk persistence.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionImageLoader
