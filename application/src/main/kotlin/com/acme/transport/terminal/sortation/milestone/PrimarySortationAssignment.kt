package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary sortation assignment is in its life cycle. */
enum class PrimarySortationAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a primary sortation assignment and its current status. */
data class PrimarySortationAssignment(
    var code: String,
    var label: String,
    var amount: Double,
    var routeCode: String,
    var fuelLiters: Double,
    var batchSize: Int,
    var status: PrimarySortationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimarySortationAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimarySortationAssignmentStatus): Boolean {
        when (status) {
            PrimarySortationAssignmentStatus.DRAFT -> return next == PrimarySortationAssignmentStatus.SUSPENDED
            PrimarySortationAssignmentStatus.PENDING -> return next == PrimarySortationAssignmentStatus.SUSPENDED
            PrimarySortationAssignmentStatus.ACTIVE -> return next == PrimarySortationAssignmentStatus.SUSPENDED || next == PrimarySortationAssignmentStatus.CLOSED
            PrimarySortationAssignmentStatus.SUSPENDED -> {
                when (next) {
                    PrimarySortationAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimarySortationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("primary sortation assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((fuelLiters + amount) * 100) / 100

    fun snapshot(): PrimarySortationAssignment {
        return PrimarySortationAssignment(
            code = code,
            label = label,
            amount = amount,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary sortation assignment in status draft. */
fun newPrimarySortationAssignment(code: String, label: String): PrimarySortationAssignment {
    return PrimarySortationAssignment(
        code = code,
        label = label,
        amount = 0.0,
        routeCode = "",
        fuelLiters = 0.0,
        batchSize = 0,
        status = PrimarySortationAssignmentStatus.DRAFT,
    )
}
