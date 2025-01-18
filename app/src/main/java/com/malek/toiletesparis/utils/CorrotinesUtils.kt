package com.malek.toiletesparis.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalContracts::class)
inline fun <T> runSuspendCatching(block: () -> T): Result<T> {
    return runCatching(block).onFailure {
        if (it is CancellationException) {
            throw it
        }
    }
}