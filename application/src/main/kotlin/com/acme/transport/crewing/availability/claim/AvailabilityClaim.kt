package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability claim records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability claim is in its life cycle. */
enum class AvailabilityClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A availability claim. Its code is unique within a tenant. */
data class AvailabilityClaim(
    var code: String,
    var label: String,
    var floorValue: Double,
    var shiftCode: String,
    var priority: Int,
    var batchSize: Int,
    var status: AvailabilityClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityClaimStatus): Boolean {
        when (status) {
            AvailabilityClaimStatus.IDLE -> return next == AvailabilityClaimStatus.WARMING || next == AvailabilityClaimStatus.READY
            AvailabilityClaimStatus.WARMING -> {
                when (next) {
                    AvailabilityClaimStatus.DRAINING, AvailabilityClaimStatus.STOPPED -> return true
                }
                return false
            }
            AvailabilityClaimStatus.READY -> {
                when (next) {
                    AvailabilityClaimStatus.DRAINING -> return true
                }
                return false
            }
            AvailabilityClaimStatus.DRAINING -> {
                when (next) {
                    AvailabilityClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityClaimStatus) {
        if (!canMoveTo(next)) {
            error("availability claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"availability claim"} $code"

    fun projectedValue(): Double = round((priority.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityClaim {
        return AvailabilityClaim(
            code = code,
            label = label,
            floorValue = floorValue,
            shiftCode = shiftCode,
            priority = priority,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a availability claim in status idle. */
fun newAvailabilityClaim(code: String, label: String): AvailabilityClaim {
    return AvailabilityClaim(
        code = code,
        label = label,
        floorValue = 0.0,
        shiftCode = "",
        priority = 0,
        batchSize = 0,
        status = AvailabilityClaimStatus.IDLE,
    )
}
