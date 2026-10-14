package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient fuel burn assignment is in its life cycle. */
enum class LenientFuelBurnAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient fuel burn assignment. Its code is unique within a tenant. */
data class LenientFuelBurnAssignment(
    var code: String,
    var title: String,
    var graceDays: Int,
    var limit: Int,
    var trailerId: String,
    var fuelLiters: Double,
    var status: LenientFuelBurnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelBurnAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelBurnAssignmentStatus): Boolean {
        when (status) {
            LenientFuelBurnAssignmentStatus.REQUESTED -> return next == LenientFuelBurnAssignmentStatus.GRANTED || next == LenientFuelBurnAssignmentStatus.REVOKED
            LenientFuelBurnAssignmentStatus.GRANTED -> {
                when (next) {
                    LenientFuelBurnAssignmentStatus.REVOKED -> return true
                }
                return false
            }
            LenientFuelBurnAssignmentStatus.REVOKED -> {
                when (next) {
                    LenientFuelBurnAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFuelBurnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel burn assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round(limit.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): LenientFuelBurnAssignment {
        return LenientFuelBurnAssignment(
            code = code,
            title = title,
            graceDays = graceDays,
            limit = limit,
            trailerId = trailerId,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient fuel burn assignment in status requested. */
fun newLenientFuelBurnAssignment(code: String, title: String): LenientFuelBurnAssignment {
    return LenientFuelBurnAssignment(
        code = code,
        title = title,
        graceDays = 0,
        limit = 0,
        trailerId = "",
        fuelLiters = 0.0,
        status = LenientFuelBurnAssignmentStatus.REQUESTED,
    )
}
