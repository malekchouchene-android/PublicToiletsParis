package com.malek.utlis

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> runSuspendCatching(block: () -> T): Result<T> {
    return runCatching(block).onFailure {
        if (it is CancellationException) {
            throw it
        }
    }
}