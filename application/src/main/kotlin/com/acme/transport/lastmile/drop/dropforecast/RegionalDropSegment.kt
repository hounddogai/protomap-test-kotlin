package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.normalize

/** Where a regional drop segment is in its life cycle. */
enum class RegionalDropSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional drop segment and its current status. */
data class RegionalDropSegment(
    var code: String,
    var name: String,
    var priority: Int,
    var routeCode: String,
    var phone: String,
    var status: RegionalDropSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDropSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDropSegmentStatus): Boolean {
        when (status) {
            RegionalDropSegmentStatus.PLANNED -> return next == RegionalDropSegmentStatus.IN_PROGRESS
            RegionalDropSegmentStatus.SCHEDULED -> {
                when (next) {
                    RegionalDropSegmentStatus.COMPLETED, RegionalDropSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDropSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalDropSegmentStatus.COMPLETED, RegionalDropSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDropSegmentStatus.COMPLETED -> return next == RegionalDropSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalDropSegmentStatus) {
        if (!canMoveTo(next)) {
            error("regional drop segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): RegionalDropSegment {
        return RegionalDropSegment(
            code = code,
            name = name,
            priority = priority,
            routeCode = routeCode,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional drop segment in status planned. */
fun newRegionalDropSegment(code: String, name: String): RegionalDropSegment {
    return RegionalDropSegment(
        code = code,
        name = name,
        priority = 0,
        routeCode = "",
        phone = "",
        status = RegionalDropSegmentStatus.PLANNED,
    )
}
