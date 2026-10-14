package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period assignment records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period assignment is in its life cycle. */
enum class DutyPeriodAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a duty period assignment and its current status. */
data class DutyPeriodAssignment(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var attempts: Int,
    var fuelLiters: Double,
    var externalRef: String,
    var batchSize: Int,
    var tags: MutableList<String>,
    var description: String,
    var amount: Double,
    var status: DutyPeriodAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodAssignmentStatus): Boolean {
        when (status) {
            DutyPeriodAssignmentStatus.PLANNED -> return next == DutyPeriodAssignmentStatus.COMPLETED || next == DutyPeriodAssignmentStatus.CANCELLED
            DutyPeriodAssignmentStatus.SCHEDULED -> {
                when (next) {
                    DutyPeriodAssignmentStatus.COMPLETED, DutyPeriodAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            DutyPeriodAssignmentStatus.IN_PROGRESS -> return next == DutyPeriodAssignmentStatus.COMPLETED
            DutyPeriodAssignmentStatus.COMPLETED -> {
                when (next) {
                    DutyPeriodAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("duty period assignment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"duty period assignment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((fuelLiters + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodAssignment {
        return DutyPeriodAssignment(
            code = code,
            label = label,
            updatedAt = updatedAt,
            attempts = attempts,
            fuelLiters = fuelLiters,
            externalRef = externalRef,
            batchSize = batchSize,
            tags = tags,
            description = description,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period assignment in status planned. */
fun newDutyPeriodAssignment(code: String, label: String): DutyPeriodAssignment {
    return DutyPeriodAssignment(
        code = code,
        label = label,
        updatedAt = 0,
        attempts = 0,
        fuelLiters = 0.0,
        externalRef = "",
        batchSize = 0,
        tags = mutableListOf<String>(),
        description = "",
        amount = 0.0,
        status = DutyPeriodAssignmentStatus.PLANNED,
    )
}
