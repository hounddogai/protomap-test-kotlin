package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dock door assignment is in its life cycle. */
enum class ManualDockDoorAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a manual dock door assignment and its current status. */
data class ManualDockDoorAssignment(
    var code: String,
    var title: String,
    var name: String,
    var sequence: Int,
    var contactEmail: String,
    var summary: String,
    var score: Double,
    var version: Int,
    var currency: String,
    var status: ManualDockDoorAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDockDoorAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDockDoorAssignmentStatus): Boolean {
        when (status) {
            ManualDockDoorAssignmentStatus.DRAFT -> {
                when (next) {
                    ManualDockDoorAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualDockDoorAssignmentStatus.PENDING -> {
                when (next) {
                    ManualDockDoorAssignmentStatus.SUSPENDED, ManualDockDoorAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            ManualDockDoorAssignmentStatus.ACTIVE -> {
                when (next) {
                    ManualDockDoorAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualDockDoorAssignmentStatus.SUSPENDED -> return next == ManualDockDoorAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualDockDoorAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual dock door assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"manual dock door assignment"} $code"

    fun adjustedExposure(): Double = round((version.toDouble() - score) * 100) / 100

    fun snapshot(): ManualDockDoorAssignment {
        return ManualDockDoorAssignment(
            code = code,
            title = title,
            name = name,
            sequence = sequence,
            contactEmail = contactEmail,
            summary = summary,
            score = score,
            version = version,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual dock door assignment in status draft. */
fun newManualDockDoorAssignment(code: String, title: String): ManualDockDoorAssignment {
    return ManualDockDoorAssignment(
        code = code,
        title = title,
        name = "",
        sequence = 0,
        contactEmail = "",
        summary = "",
        score = 0.0,
        version = 0,
        currency = "",
        status = ManualDockDoorAssignmentStatus.DRAFT,
    )
}
