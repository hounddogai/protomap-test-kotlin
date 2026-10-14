package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback qualification slot records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a fallback qualification slot is in its life cycle. */
enum class FallbackQualificationSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback qualification slot, with what the planners need to act on it. */
data class FallbackQualificationSlot(
    var code: String,
    var title: String,
    var revision: Int,
    var graceDays: Int,
    var floorValue: Double,
    var driverId: String,
    var notes: String,
    var threshold: Double,
    var trailerId: String,
    var active: Boolean,
    var summary: String,
    var status: FallbackQualificationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackQualificationSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackQualificationSlotStatus): Boolean {
        when (status) {
            FallbackQualificationSlotStatus.IDLE -> {
                when (next) {
                    FallbackQualificationSlotStatus.WARMING, FallbackQualificationSlotStatus.DRAINING -> return true
                }
                return false
            }
            FallbackQualificationSlotStatus.WARMING -> {
                when (next) {
                    FallbackQualificationSlotStatus.DRAINING, FallbackQualificationSlotStatus.STOPPED -> return true
                }
                return false
            }
            FallbackQualificationSlotStatus.READY -> {
                when (next) {
                    FallbackQualificationSlotStatus.DRAINING, FallbackQualificationSlotStatus.STOPPED -> return true
                }
                return false
            }
            FallbackQualificationSlotStatus.DRAINING -> {
                when (next) {
                    FallbackQualificationSlotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackQualificationSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback qualification slot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun netExposure(): Double = round((graceDays.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): FallbackQualificationSlot {
        return FallbackQualificationSlot(
            code = code,
            title = title,
            revision = revision,
            graceDays = graceDays,
            floorValue = floorValue,
            driverId = driverId,
            notes = notes,
            threshold = threshold,
            trailerId = trailerId,
            active = active,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback qualification slot in status idle. */
fun newFallbackQualificationSlot(code: String, title: String): FallbackQualificationSlot {
    return FallbackQualificationSlot(
        code = code,
        title = title,
        revision = 0,
        graceDays = 0,
        floorValue = 0.0,
        driverId = "",
        notes = "",
        threshold = 0.0,
        trailerId = "",
        active = false,
        summary = "",
        status = FallbackQualificationSlotStatus.IDLE,
    )
}
