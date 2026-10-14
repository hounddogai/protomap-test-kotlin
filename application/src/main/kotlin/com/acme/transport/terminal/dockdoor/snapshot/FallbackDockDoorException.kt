package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dock door exception is in its life cycle. */
enum class FallbackDockDoorExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback dock door exception as the dock door team keeps it. */
data class FallbackDockDoorException(
    var code: String,
    var name: String,
    var threshold: Double,
    var retries: Int,
    var reviewer: String,
    var lane: String,
    var margin: Double,
    var discount: Double,
    var segment: String,
    var status: FallbackDockDoorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDockDoorExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDockDoorExceptionStatus): Boolean {
        when (status) {
            FallbackDockDoorExceptionStatus.PLANNED -> {
                when (next) {
                    FallbackDockDoorExceptionStatus.COMPLETED, FallbackDockDoorExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackDockDoorExceptionStatus.SCHEDULED -> return next == FallbackDockDoorExceptionStatus.COMPLETED
            FallbackDockDoorExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackDockDoorExceptionStatus.COMPLETED, FallbackDockDoorExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackDockDoorExceptionStatus.COMPLETED -> {
                when (next) {
                    FallbackDockDoorExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDockDoorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("fallback dock door exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback dock door exception"} $code"

    fun projectedVolume(): Double = round((retries.toDouble() + margin) * 100) / 100

    fun snapshot(): FallbackDockDoorException {
        return FallbackDockDoorException(
            code = code,
            name = name,
            threshold = threshold,
            retries = retries,
            reviewer = reviewer,
            lane = lane,
            margin = margin,
            discount = discount,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback dock door exception in status planned. */
fun newFallbackDockDoorException(code: String, name: String): FallbackDockDoorException {
    return FallbackDockDoorException(
        code = code,
        name = name,
        threshold = 0.0,
        retries = 0,
        reviewer = "",
        lane = "",
        margin = 0.0,
        discount = 0.0,
        segment = "",
        status = FallbackDockDoorExceptionStatus.PLANNED,
    )
}
