package com.acme.transport.crewing.availability.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability request records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability request is in its life cycle. */
enum class AvailabilityRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a availability request and its current status. */
data class AvailabilityRequest(
    var code: String,
    var label: String,
    var active: Boolean,
    var stopCount: Int,
    var weight: Double,
    var status: AvailabilityRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityRequestStatus): Boolean {
        when (status) {
            AvailabilityRequestStatus.IDLE -> {
                when (next) {
                    AvailabilityRequestStatus.READY -> return true
                }
                return false
            }
            AvailabilityRequestStatus.WARMING -> {
                when (next) {
                    AvailabilityRequestStatus.DRAINING, AvailabilityRequestStatus.STOPPED -> return true
                }
                return false
            }
            AvailabilityRequestStatus.READY -> return next == AvailabilityRequestStatus.DRAINING || next == AvailabilityRequestStatus.STOPPED
            AvailabilityRequestStatus.DRAINING -> return next == AvailabilityRequestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AvailabilityRequestStatus) {
        if (!canMoveTo(next)) {
            error("availability request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"availability request"} $code"

    fun weightedTotal(): Double = round(weight * stopCount.toDouble() * 100) / 100

    fun snapshot(): AvailabilityRequest {
        return AvailabilityRequest(
            code = code,
            label = label,
            active = active,
            stopCount = stopCount,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a availability request in status idle. */
fun newAvailabilityRequest(code: String, label: String): AvailabilityRequest {
    return AvailabilityRequest(
        code = code,
        label = label,
        active = false,
        stopCount = 0,
        weight = 0.0,
        status = AvailabilityRequestStatus.IDLE,
    )
}
