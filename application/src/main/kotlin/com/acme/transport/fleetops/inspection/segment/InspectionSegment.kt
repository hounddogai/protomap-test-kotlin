package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.text.normalize

// The inspection segment records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection segment is in its life cycle. */
enum class InspectionSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A inspection segment as the inspection team keeps it. */
data class InspectionSegment(
    var code: String,
    var label: String,
    var approved: Boolean,
    var startsAt: Int,
    var active: Boolean,
    var archived: Boolean,
    var visible: Boolean,
    var routeCode: String,
    var status: InspectionSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionSegmentStatus): Boolean {
        when (status) {
            InspectionSegmentStatus.NEW -> {
                when (next) {
                    InspectionSegmentStatus.DONE -> return true
                }
                return false
            }
            InspectionSegmentStatus.QUEUED -> {
                when (next) {
                    InspectionSegmentStatus.RUNNING, InspectionSegmentStatus.DONE -> return true
                }
                return false
            }
            InspectionSegmentStatus.RUNNING -> {
                when (next) {
                    InspectionSegmentStatus.FAILED -> return true
                }
                return false
            }
            InspectionSegmentStatus.DONE -> return next == InspectionSegmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: InspectionSegmentStatus) {
        if (!canMoveTo(next)) {
            error("inspection segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"inspection segment"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): InspectionSegment {
        return InspectionSegment(
            code = code,
            label = label,
            approved = approved,
            startsAt = startsAt,
            active = active,
            archived = archived,
            visible = visible,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection segment in status new. */
fun newInspectionSegment(code: String, label: String): InspectionSegment {
    return InspectionSegment(
        code = code,
        label = label,
        approved = false,
        startsAt = 0,
        active = false,
        archived = false,
        visible = false,
        routeCode = "",
        status = InspectionSegmentStatus.NEW,
    )
}
