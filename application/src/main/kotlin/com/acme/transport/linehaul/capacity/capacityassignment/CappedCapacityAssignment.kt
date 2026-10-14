package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped capacity assignment records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a capped capacity assignment is in its life cycle. */
enum class CappedCapacityAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped capacity assignment and its current status. */
data class CappedCapacityAssignment(
    var code: String,
    var name: String,
    var graceDays: Int,
    var category: String,
    var trailerId: String,
    var externalRef: String,
    var region: String,
    var odometer: Int,
    var windowDays: Int,
    var visible: Boolean,
    var status: CappedCapacityAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCapacityAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCapacityAssignmentStatus): Boolean {
        when (status) {
            CappedCapacityAssignmentStatus.PROPOSED -> return next == CappedCapacityAssignmentStatus.REJECTED
            CappedCapacityAssignmentStatus.APPROVED -> {
                when (next) {
                    CappedCapacityAssignmentStatus.REJECTED, CappedCapacityAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedCapacityAssignmentStatus.REJECTED -> {
                when (next) {
                    CappedCapacityAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCapacityAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("capped capacity assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round((windowDays.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedCapacityAssignment {
        return CappedCapacityAssignment(
            code = code,
            name = name,
            graceDays = graceDays,
            category = category,
            trailerId = trailerId,
            externalRef = externalRef,
            region = region,
            odometer = odometer,
            windowDays = windowDays,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped capacity assignment in status proposed. */
fun newCappedCapacityAssignment(code: String, name: String): CappedCapacityAssignment {
    return CappedCapacityAssignment(
        code = code,
        name = name,
        graceDays = 0,
        category = "",
        trailerId = "",
        externalRef = "",
        region = "",
        odometer = 0,
        windowDays = 0,
        visible = false,
        status = CappedCapacityAssignmentStatus.PROPOSED,
    )
}
