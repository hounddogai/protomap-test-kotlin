package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual availability segment records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a manual availability segment is in its life cycle. */
enum class ManualAvailabilitySegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a manual availability segment and its current status. */
data class ManualAvailabilitySegment(
    var code: String,
    var label: String,
    var phone: String,
    var priority: Int,
    var shiftCode: String,
    var externalRef: String,
    var cost: Double,
    var windowDays: Int,
    var odometer: Int,
    var status: ManualAvailabilitySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAvailabilitySegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAvailabilitySegmentStatus): Boolean {
        when (status) {
            ManualAvailabilitySegmentStatus.OPEN -> return next == ManualAvailabilitySegmentStatus.RESOLVED
            ManualAvailabilitySegmentStatus.IN_REVIEW -> {
                when (next) {
                    ManualAvailabilitySegmentStatus.RESOLVED -> return true
                }
                return false
            }
            ManualAvailabilitySegmentStatus.RESOLVED -> return next == ManualAvailabilitySegmentStatus.REOPENED || next == ManualAvailabilitySegmentStatus.ARCHIVED
            ManualAvailabilitySegmentStatus.REOPENED -> {
                when (next) {
                    ManualAvailabilitySegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAvailabilitySegmentStatus) {
        if (!canMoveTo(next)) {
            error("manual availability segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round(odometer.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): ManualAvailabilitySegment {
        return ManualAvailabilitySegment(
            code = code,
            label = label,
            phone = phone,
            priority = priority,
            shiftCode = shiftCode,
            externalRef = externalRef,
            cost = cost,
            windowDays = windowDays,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual availability segment in status open. */
fun newManualAvailabilitySegment(code: String, label: String): ManualAvailabilitySegment {
    return ManualAvailabilitySegment(
        code = code,
        label = label,
        phone = "",
        priority = 0,
        shiftCode = "",
        externalRef = "",
        cost = 0.0,
        windowDays = 0,
        odometer = 0,
        status = ManualAvailabilitySegmentStatus.OPEN,
    )
}
