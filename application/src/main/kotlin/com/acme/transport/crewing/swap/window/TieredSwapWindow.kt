package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered swap window records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a tiered swap window is in its life cycle. */
enum class TieredSwapWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered swap window, with what the planners need to act on it. */
data class TieredSwapWindow(
    var code: String,
    var label: String,
    var version: Int,
    var durationMinutes: Int,
    var stopCount: Int,
    var discount: Double,
    var trailerId: String,
    var threshold: Double,
    var externalRef: String,
    var checksum: String,
    var status: TieredSwapWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSwapWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSwapWindowStatus): Boolean {
        when (status) {
            TieredSwapWindowStatus.IDLE -> {
                when (next) {
                    TieredSwapWindowStatus.STOPPED -> return true
                }
                return false
            }
            TieredSwapWindowStatus.WARMING -> return next == TieredSwapWindowStatus.READY || next == TieredSwapWindowStatus.DRAINING
            TieredSwapWindowStatus.READY -> return next == TieredSwapWindowStatus.STOPPED
            TieredSwapWindowStatus.DRAINING -> {
                when (next) {
                    TieredSwapWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSwapWindowStatus) {
        if (!canMoveTo(next)) {
            error("tiered swap window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"tiered swap window"} $code"

    fun effectiveExposure(): Double = round((stopCount.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): TieredSwapWindow {
        return TieredSwapWindow(
            code = code,
            label = label,
            version = version,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            discount = discount,
            trailerId = trailerId,
            threshold = threshold,
            externalRef = externalRef,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered swap window in status idle. */
fun newTieredSwapWindow(code: String, label: String): TieredSwapWindow {
    return TieredSwapWindow(
        code = code,
        label = label,
        version = 0,
        durationMinutes = 0,
        stopCount = 0,
        discount = 0.0,
        trailerId = "",
        threshold = 0.0,
        externalRef = "",
        checksum = "",
        status = TieredSwapWindowStatus.IDLE,
    )
}
