package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient inspection slot records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a lenient inspection slot is in its life cycle. */
enum class LenientInspectionSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient inspection slot as the inspection team keeps it. */
data class LenientInspectionSlot(
    var code: String,
    var label: String,
    var startsAt: Int,
    var approved: Boolean,
    var weight: Double,
    var odometer: Int,
    var retries: Int,
    var sequence: Int,
    var distanceKm: Double,
    var reviewer: String,
    var expiresAt: Int,
    var status: LenientInspectionSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientInspectionSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientInspectionSlotStatus): Boolean {
        when (status) {
            LenientInspectionSlotStatus.PLANNED -> {
                when (next) {
                    LenientInspectionSlotStatus.SCHEDULED, LenientInspectionSlotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LenientInspectionSlotStatus.SCHEDULED -> {
                when (next) {
                    LenientInspectionSlotStatus.CANCELLED -> return true
                }
                return false
            }
            LenientInspectionSlotStatus.IN_PROGRESS -> return next == LenientInspectionSlotStatus.COMPLETED
            LenientInspectionSlotStatus.COMPLETED -> return next == LenientInspectionSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientInspectionSlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient inspection slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedTotal(): Double = round((weight + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): LenientInspectionSlot {
        return LenientInspectionSlot(
            code = code,
            label = label,
            startsAt = startsAt,
            approved = approved,
            weight = weight,
            odometer = odometer,
            retries = retries,
            sequence = sequence,
            distanceKm = distanceKm,
            reviewer = reviewer,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient inspection slot in status planned. */
fun newLenientInspectionSlot(code: String, label: String): LenientInspectionSlot {
    return LenientInspectionSlot(
        code = code,
        label = label,
        startsAt = 0,
        approved = false,
        weight = 0.0,
        odometer = 0,
        retries = 0,
        sequence = 0,
        distanceKm = 0.0,
        reviewer = "",
        expiresAt = 0,
        status = LenientInspectionSlotStatus.PLANNED,
    )
}
