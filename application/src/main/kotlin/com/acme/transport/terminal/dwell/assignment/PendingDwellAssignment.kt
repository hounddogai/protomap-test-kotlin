package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending dwell assignment records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a pending dwell assignment is in its life cycle. */
enum class PendingDwellAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending dwell assignment as the dwell team keeps it. */
data class PendingDwellAssignment(
    var code: String,
    var title: String,
    var country: String,
    var quantity: Int,
    var loadKg: Double,
    var sequence: Int,
    var pallets: Int,
    var dock: String,
    var status: PendingDwellAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDwellAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDwellAssignmentStatus): Boolean {
        when (status) {
            PendingDwellAssignmentStatus.REQUESTED -> return next == PendingDwellAssignmentStatus.REVOKED || next == PendingDwellAssignmentStatus.EXPIRED
            PendingDwellAssignmentStatus.GRANTED -> {
                when (next) {
                    PendingDwellAssignmentStatus.REVOKED, PendingDwellAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            PendingDwellAssignmentStatus.REVOKED -> return next == PendingDwellAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingDwellAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending dwell assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending dwell assignment"} $code"

    fun adjustedLoad(): Double = round((loadKg - quantity.toDouble()) * 100) / 100

    fun snapshot(): PendingDwellAssignment {
        return PendingDwellAssignment(
            code = code,
            title = title,
            country = country,
            quantity = quantity,
            loadKg = loadKg,
            sequence = sequence,
            pallets = pallets,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending dwell assignment in status requested. */
fun newPendingDwellAssignment(code: String, title: String): PendingDwellAssignment {
    return PendingDwellAssignment(
        code = code,
        title = title,
        country = "",
        quantity = 0,
        loadKg = 0.0,
        sequence = 0,
        pallets = 0,
        dock = "",
        status = PendingDwellAssignmentStatus.REQUESTED,
    )
}
