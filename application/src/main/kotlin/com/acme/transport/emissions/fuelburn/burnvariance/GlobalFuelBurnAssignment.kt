package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel burn assignment is in its life cycle. */
enum class GlobalFuelBurnAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global fuel burn assignment as the fuel burn team keeps it. */
data class GlobalFuelBurnAssignment(
    var code: String,
    var name: String,
    var margin: Double,
    var expiresAt: Int,
    var revision: Int,
    var pallets: Int,
    var status: GlobalFuelBurnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelBurnAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelBurnAssignmentStatus): Boolean {
        when (status) {
            GlobalFuelBurnAssignmentStatus.REQUESTED -> return next == GlobalFuelBurnAssignmentStatus.GRANTED
            GlobalFuelBurnAssignmentStatus.GRANTED -> return next == GlobalFuelBurnAssignmentStatus.REVOKED || next == GlobalFuelBurnAssignmentStatus.EXPIRED
            GlobalFuelBurnAssignmentStatus.REVOKED -> {
                when (next) {
                    GlobalFuelBurnAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalFuelBurnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("global fuel burn assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round(revision.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): GlobalFuelBurnAssignment {
        return GlobalFuelBurnAssignment(
            code = code,
            name = name,
            margin = margin,
            expiresAt = expiresAt,
            revision = revision,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global fuel burn assignment in status requested. */
fun newGlobalFuelBurnAssignment(code: String, name: String): GlobalFuelBurnAssignment {
    return GlobalFuelBurnAssignment(
        code = code,
        name = name,
        margin = 0.0,
        expiresAt = 0,
        revision = 0,
        pallets = 0,
        status = GlobalFuelBurnAssignmentStatus.REQUESTED,
    )
}
