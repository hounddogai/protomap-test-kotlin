package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual idling assignment is in its life cycle. */
enum class ManualIdlingAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual idling assignment as the idling team keeps it. */
data class ManualIdlingAssignment(
    var code: String,
    var title: String,
    var attempts: Int,
    var lane: String,
    var loadKg: Double,
    var status: ManualIdlingAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIdlingAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIdlingAssignmentStatus): Boolean {
        when (status) {
            ManualIdlingAssignmentStatus.IDLE -> {
                when (next) {
                    ManualIdlingAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            ManualIdlingAssignmentStatus.WARMING -> return next == ManualIdlingAssignmentStatus.STOPPED
            ManualIdlingAssignmentStatus.READY -> {
                when (next) {
                    ManualIdlingAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            ManualIdlingAssignmentStatus.DRAINING -> return next == ManualIdlingAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualIdlingAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual idling assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((attempts.toDouble() - loadKg) * 100) / 100

    fun snapshot(): ManualIdlingAssignment {
        return ManualIdlingAssignment(
            code = code,
            title = title,
            attempts = attempts,
            lane = lane,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual idling assignment in status idle. */
fun newManualIdlingAssignment(code: String, title: String): ManualIdlingAssignment {
    return ManualIdlingAssignment(
        code = code,
        title = title,
        attempts = 0,
        lane = "",
        loadKg = 0.0,
        status = ManualIdlingAssignmentStatus.IDLE,
    )
}
