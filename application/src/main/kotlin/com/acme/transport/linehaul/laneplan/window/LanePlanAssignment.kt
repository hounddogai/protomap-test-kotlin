package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.normalize

/** Where a lane plan assignment is in its life cycle. */
enum class LanePlanAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lane plan assignment. Its code is unique within a tenant. */
data class LanePlanAssignment(
    var code: String,
    var name: String,
    var checksum: String,
    var windowDays: Int,
    var source: String,
    var routeCode: String,
    var status: LanePlanAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanAssignmentStatus): Boolean {
        when (status) {
            LanePlanAssignmentStatus.IDLE -> return next == LanePlanAssignmentStatus.READY || next == LanePlanAssignmentStatus.STOPPED
            LanePlanAssignmentStatus.WARMING -> {
                when (next) {
                    LanePlanAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            LanePlanAssignmentStatus.READY -> {
                when (next) {
                    LanePlanAssignmentStatus.DRAINING, LanePlanAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            LanePlanAssignmentStatus.DRAINING -> {
                when (next) {
                    LanePlanAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lane plan assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lane plan assignment"} $code"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): LanePlanAssignment {
        return LanePlanAssignment(
            code = code,
            name = name,
            checksum = checksum,
            windowDays = windowDays,
            source = source,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lane plan assignment in status idle. */
fun newLanePlanAssignment(code: String, name: String): LanePlanAssignment {
    return LanePlanAssignment(
        code = code,
        name = name,
        checksum = "",
        windowDays = 0,
        source = "",
        routeCode = "",
        status = LanePlanAssignmentStatus.IDLE,
    )
}
