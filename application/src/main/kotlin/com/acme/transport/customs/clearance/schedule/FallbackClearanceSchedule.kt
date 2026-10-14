package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback clearance schedule records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a fallback clearance schedule is in its life cycle. */
enum class FallbackClearanceScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback clearance schedule, with what the planners need to act on it. */
data class FallbackClearanceSchedule(
    var code: String,
    var title: String,
    var contactEmail: String,
    var label: String,
    var floorValue: Double,
    var carrier: String,
    var weight: Double,
    var limit: Int,
    var region: String,
    var status: FallbackClearanceScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackClearanceScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackClearanceScheduleStatus): Boolean {
        when (status) {
            FallbackClearanceScheduleStatus.OPEN -> return next == FallbackClearanceScheduleStatus.ARCHIVED
            FallbackClearanceScheduleStatus.IN_REVIEW -> {
                when (next) {
                    FallbackClearanceScheduleStatus.REOPENED, FallbackClearanceScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackClearanceScheduleStatus.RESOLVED -> {
                when (next) {
                    FallbackClearanceScheduleStatus.REOPENED, FallbackClearanceScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackClearanceScheduleStatus.REOPENED -> return next == FallbackClearanceScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackClearanceScheduleStatus) {
        if (!canMoveTo(next)) {
            error("fallback clearance schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun adjustedVolume(): Double = round((weight - limit.toDouble()) * 100) / 100

    fun snapshot(): FallbackClearanceSchedule {
        return FallbackClearanceSchedule(
            code = code,
            title = title,
            contactEmail = contactEmail,
            label = label,
            floorValue = floorValue,
            carrier = carrier,
            weight = weight,
            limit = limit,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback clearance schedule in status open. */
fun newFallbackClearanceSchedule(code: String, title: String): FallbackClearanceSchedule {
    return FallbackClearanceSchedule(
        code = code,
        title = title,
        contactEmail = "",
        label = "",
        floorValue = 0.0,
        carrier = "",
        weight = 0.0,
        limit = 0,
        region = "",
        status = FallbackClearanceScheduleStatus.OPEN,
    )
}
