package com.acme.transport.customs.hold.holdslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient hold slot records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a lenient hold slot is in its life cycle. */
enum class LenientHoldSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient hold slot and its current status. */
data class LenientHoldSlot(
    var code: String,
    var name: String,
    var notes: String,
    var budget: Double,
    var distanceKm: Double,
    var fuelLiters: Double,
    var stopCount: Int,
    var lane: String,
    var trailerId: String,
    var status: LenientHoldSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHoldSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHoldSlotStatus): Boolean {
        when (status) {
            LenientHoldSlotStatus.OPEN -> {
                when (next) {
                    LenientHoldSlotStatus.IN_REVIEW, LenientHoldSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientHoldSlotStatus.IN_REVIEW -> return next == LenientHoldSlotStatus.ARCHIVED
            LenientHoldSlotStatus.RESOLVED -> return next == LenientHoldSlotStatus.REOPENED
            LenientHoldSlotStatus.REOPENED -> {
                when (next) {
                    LenientHoldSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientHoldSlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient hold slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((fuelLiters + stopCount.toDouble()) * 100) / 100

    fun snapshot(): LenientHoldSlot {
        return LenientHoldSlot(
            code = code,
            name = name,
            notes = notes,
            budget = budget,
            distanceKm = distanceKm,
            fuelLiters = fuelLiters,
            stopCount = stopCount,
            lane = lane,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient hold slot in status open. */
fun newLenientHoldSlot(code: String, name: String): LenientHoldSlot {
    return LenientHoldSlot(
        code = code,
        name = name,
        notes = "",
        budget = 0.0,
        distanceKm = 0.0,
        fuelLiters = 0.0,
        stopCount = 0,
        lane = "",
        trailerId = "",
        status = LenientHoldSlotStatus.OPEN,
    )
}
