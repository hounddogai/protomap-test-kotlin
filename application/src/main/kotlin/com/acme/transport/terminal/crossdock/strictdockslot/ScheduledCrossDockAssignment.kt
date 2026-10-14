package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled cross dock assignment is in its life cycle. */
enum class ScheduledCrossDockAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled cross dock assignment and its current status. */
data class ScheduledCrossDockAssignment(
    var code: String,
    var title: String,
    var discount: Double,
    var region: String,
    var version: Int,
    var pallets: Int,
    var status: ScheduledCrossDockAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCrossDockAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCrossDockAssignmentStatus): Boolean {
        when (status) {
            ScheduledCrossDockAssignmentStatus.REQUESTED -> {
                when (next) {
                    ScheduledCrossDockAssignmentStatus.REVOKED, ScheduledCrossDockAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledCrossDockAssignmentStatus.GRANTED -> {
                when (next) {
                    ScheduledCrossDockAssignmentStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledCrossDockAssignmentStatus.REVOKED -> return next == ScheduledCrossDockAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledCrossDockAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled cross dock assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"scheduled cross dock assignment"} $code"

    fun projectedTotal(): Double = round(discount * version.toDouble() * 100) / 100

    fun snapshot(): ScheduledCrossDockAssignment {
        return ScheduledCrossDockAssignment(
            code = code,
            title = title,
            discount = discount,
            region = region,
            version = version,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled cross dock assignment in status requested. */
fun newScheduledCrossDockAssignment(code: String, title: String): ScheduledCrossDockAssignment {
    return ScheduledCrossDockAssignment(
        code = code,
        title = title,
        discount = 0.0,
        region = "",
        version = 0,
        pallets = 0,
        status = ScheduledCrossDockAssignmentStatus.REQUESTED,
    )
}
