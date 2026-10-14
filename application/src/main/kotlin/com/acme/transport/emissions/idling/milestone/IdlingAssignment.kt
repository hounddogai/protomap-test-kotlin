package com.acme.transport.emissions.idling.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling assignment is in its life cycle. */
enum class IdlingAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One idling assignment, with what the planners need to act on it. */
data class IdlingAssignment(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var locale: String,
    var visible: Boolean,
    var segment: String,
    var pinned: Boolean,
    var stopCount: Int,
    var minimum: Double,
    var reviewer: String,
    var phone: String,
    var status: IdlingAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingAssignmentStatus): Boolean {
        when (status) {
            IdlingAssignmentStatus.PLANNED -> {
                when (next) {
                    IdlingAssignmentStatus.SCHEDULED, IdlingAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            IdlingAssignmentStatus.SCHEDULED -> return next == IdlingAssignmentStatus.IN_PROGRESS || next == IdlingAssignmentStatus.COMPLETED
            IdlingAssignmentStatus.IN_PROGRESS -> return next == IdlingAssignmentStatus.COMPLETED || next == IdlingAssignmentStatus.CANCELLED
            IdlingAssignmentStatus.COMPLETED -> {
                when (next) {
                    IdlingAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("idling assignment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"idling assignment"} $code"

    fun effectiveLoad(): Double = round((updatedAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): IdlingAssignment {
        return IdlingAssignment(
            code = code,
            label = label,
            updatedAt = updatedAt,
            locale = locale,
            visible = visible,
            segment = segment,
            pinned = pinned,
            stopCount = stopCount,
            minimum = minimum,
            reviewer = reviewer,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a idling assignment in status planned. */
fun newIdlingAssignment(code: String, label: String): IdlingAssignment {
    return IdlingAssignment(
        code = code,
        label = label,
        updatedAt = 0,
        locale = "",
        visible = false,
        segment = "",
        pinned = false,
        stopCount = 0,
        minimum = 0.0,
        reviewer = "",
        phone = "",
        status = IdlingAssignmentStatus.PLANNED,
    )
}
