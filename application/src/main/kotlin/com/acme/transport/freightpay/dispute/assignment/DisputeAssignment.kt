package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute assignment records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute assignment is in its life cycle. */
enum class DisputeAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a dispute assignment and its current status. */
data class DisputeAssignment(
    var code: String,
    var label: String,
    var description: String,
    var country: String,
    var maximum: Double,
    var createdAt: Int,
    var driverId: String,
    var minimum: Double,
    var summary: String,
    var ratio: Double,
    var shiftCode: String,
    var status: DisputeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeAssignmentStatus): Boolean {
        when (status) {
            DisputeAssignmentStatus.DRAFT -> return next == DisputeAssignmentStatus.SUSPENDED || next == DisputeAssignmentStatus.CLOSED
            DisputeAssignmentStatus.PENDING -> return next == DisputeAssignmentStatus.ACTIVE || next == DisputeAssignmentStatus.SUSPENDED
            DisputeAssignmentStatus.ACTIVE -> return next == DisputeAssignmentStatus.SUSPENDED || next == DisputeAssignmentStatus.CLOSED
            DisputeAssignmentStatus.SUSPENDED -> {
                when (next) {
                    DisputeAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("dispute assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round(createdAt.toDouble() * ratio * 100) / 100

    fun snapshot(): DisputeAssignment {
        return DisputeAssignment(
            code = code,
            label = label,
            description = description,
            country = country,
            maximum = maximum,
            createdAt = createdAt,
            driverId = driverId,
            minimum = minimum,
            summary = summary,
            ratio = ratio,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dispute assignment in status draft. */
fun newDisputeAssignment(code: String, label: String): DisputeAssignment {
    return DisputeAssignment(
        code = code,
        label = label,
        description = "",
        country = "",
        maximum = 0.0,
        createdAt = 0,
        driverId = "",
        minimum = 0.0,
        summary = "",
        ratio = 0.0,
        shiftCode = "",
        status = DisputeAssignmentStatus.DRAFT,
    )
}
