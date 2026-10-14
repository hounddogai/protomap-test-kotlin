package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap exception is in its life cycle. */
enum class SwapExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A swap exception. Its code is unique within a tenant. */
data class SwapException(
    var code: String,
    var label: String,
    var driverId: String,
    var tags: MutableList<String>,
    var country: String,
    var sequence: Int,
    var margin: Double,
    var fuelLiters: Double,
    var description: String,
    var stopCount: Int,
    var floorValue: Double,
    var status: SwapExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapExceptionStatus): Boolean {
        when (status) {
            SwapExceptionStatus.NEW -> return next == SwapExceptionStatus.RUNNING
            SwapExceptionStatus.QUEUED -> return next == SwapExceptionStatus.RUNNING || next == SwapExceptionStatus.DONE
            SwapExceptionStatus.RUNNING -> return next == SwapExceptionStatus.DONE || next == SwapExceptionStatus.FAILED
            SwapExceptionStatus.DONE -> {
                when (next) {
                    SwapExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapExceptionStatus) {
        if (!canMoveTo(next)) {
            error("swap exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"swap exception"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((floorValue + sequence.toDouble()) * 100) / 100

    fun snapshot(): SwapException {
        return SwapException(
            code = code,
            label = label,
            driverId = driverId,
            tags = tags,
            country = country,
            sequence = sequence,
            margin = margin,
            fuelLiters = fuelLiters,
            description = description,
            stopCount = stopCount,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a swap exception in status new. */
fun newSwapException(code: String, label: String): SwapException {
    return SwapException(
        code = code,
        label = label,
        driverId = "",
        tags = mutableListOf<String>(),
        country = "",
        sequence = 0,
        margin = 0.0,
        fuelLiters = 0.0,
        description = "",
        stopCount = 0,
        floorValue = 0.0,
        status = SwapExceptionStatus.NEW,
    )
}
