package com.rhinepereira.saints.data.remote

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException

/**
 * Hard ceiling on a network call. OkHttp's own timeouts cannot interrupt a stuck DNS lookup,
 * so a dead Wi-Fi would otherwise spin forever; this surfaces it as an ordinary IOException.
 */
suspend fun <T> net(block: suspend () -> T): T = try {
    withTimeout(15_000) { block() }
} catch (e: TimeoutCancellationException) {
    throw IOException("Network timed out", e)
}

/** Serve whatever the HTTP cache holds, however old. */
const val CACHE_ONLY = "public, only-if-cached, max-stale=31536000"
