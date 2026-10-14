package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel assignment is in its life cycle. */
enum class ParcelAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One parcel assignment, with what the planners need to act on it. */
data class ParcelAssignment(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var durationMinutes: Int,
    var phone: String,
    var startsAt: Int,
    var priority: Int,
    var maximum: Double,
    var status: ParcelAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelAssignmentStatus): Boolean {
        when (status) {
            ParcelAssignmentStatus.PROPOSED -> {
                when (next) {
                    ParcelAssignmentStatus.REJECTED -> return true
                }
                return false
            }
            ParcelAssignmentStatus.APPROVED -> return next == ParcelAssignmentStatus.REJECTED || next == ParcelAssignmentStatus.WITHDRAWN
            ParcelAssignmentStatus.REJECTED -> return next == ParcelAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ParcelAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("parcel assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedLoad(): Double = round((durationMinutes.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelAssignment {
        return ParcelAssignment(
            code = code,
            title = title,
            pinned = pinned,
            durationMinutes = durationMinutes,
            phone = phone,
            startsAt = startsAt,
            priority = priority,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a parcel assignment in status proposed. */
fun newParcelAssignment(code: String, title: String): ParcelAssignment {
    return ParcelAssignment(
        code = code,
        title = title,
        pinned = false,
        durationMinutes = 0,
        phone = "",
        startsAt = 0,
        priority = 0,
        maximum = 0.0,
        status = ParcelAssignmentStatus.PROPOSED,
    )
}
