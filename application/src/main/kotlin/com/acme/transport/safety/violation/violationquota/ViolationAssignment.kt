package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation assignment is in its life cycle. */
enum class ViolationAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A violation assignment. Its code is unique within a tenant. */
data class ViolationAssignment(
    var code: String,
    var label: String,
    var threshold: Double,
    var attempts: Int,
    var currency: String,
    var active: Boolean,
    var sequence: Int,
    var segment: String,
    var status: ViolationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationAssignmentStatus): Boolean {
        when (status) {
            ViolationAssignmentStatus.CREATED -> {
                when (next) {
                    ViolationAssignmentStatus.VALIDATED, ViolationAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            ViolationAssignmentStatus.VALIDATED -> return next == ViolationAssignmentStatus.PUBLISHED || next == ViolationAssignmentStatus.RETIRED
            ViolationAssignmentStatus.PUBLISHED -> {
                when (next) {
                    ViolationAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("violation assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((sequence.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): ViolationAssignment {
        return ViolationAssignment(
            code = code,
            label = label,
            threshold = threshold,
            attempts = attempts,
            currency = currency,
            active = active,
            sequence = sequence,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a violation assignment in status created. */
fun newViolationAssignment(code: String, label: String): ViolationAssignment {
    return ViolationAssignment(
        code = code,
        label = label,
        threshold = 0.0,
        attempts = 0,
        currency = "",
        active = false,
        sequence = 0,
        segment = "",
        status = ViolationAssignmentStatus.CREATED,
    )
}
