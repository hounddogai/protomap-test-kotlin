package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered declaration assignment is in its life cycle. */
enum class TieredDeclarationAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered declaration assignment and its current status. */
data class TieredDeclarationAssignment(
    var code: String,
    var label: String,
    var depot: String,
    var limit: Int,
    var odometer: Int,
    var status: TieredDeclarationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationAssignmentStatus): Boolean {
        when (status) {
            TieredDeclarationAssignmentStatus.IDLE -> return next == TieredDeclarationAssignmentStatus.READY || next == TieredDeclarationAssignmentStatus.STOPPED
            TieredDeclarationAssignmentStatus.WARMING -> return next == TieredDeclarationAssignmentStatus.READY
            TieredDeclarationAssignmentStatus.READY -> {
                when (next) {
                    TieredDeclarationAssignmentStatus.DRAINING, TieredDeclarationAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            TieredDeclarationAssignmentStatus.DRAINING -> {
                when (next) {
                    TieredDeclarationAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDeclarationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered declaration assignment"} $code"

    fun netVolume(): Double = round((odometer.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): TieredDeclarationAssignment {
        return TieredDeclarationAssignment(
            code = code,
            label = label,
            depot = depot,
            limit = limit,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered declaration assignment in status idle. */
fun newTieredDeclarationAssignment(code: String, label: String): TieredDeclarationAssignment {
    return TieredDeclarationAssignment(
        code = code,
        label = label,
        depot = "",
        limit = 0,
        odometer = 0,
        status = TieredDeclarationAssignmentStatus.IDLE,
    )
}
