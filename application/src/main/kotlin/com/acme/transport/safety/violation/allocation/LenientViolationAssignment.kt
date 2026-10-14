package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient violation assignment is in its life cycle. */
enum class LenientViolationAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient violation assignment as the violation team keeps it. */
data class LenientViolationAssignment(
    var code: String,
    var label: String,
    var createdAt: Int,
    var sequence: Int,
    var distanceKm: Double,
    var discount: Double,
    var score: Double,
    var priority: Int,
    var pallets: Int,
    var windowDays: Int,
    var status: LenientViolationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientViolationAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientViolationAssignmentStatus): Boolean {
        when (status) {
            LenientViolationAssignmentStatus.CREATED -> {
                when (next) {
                    LenientViolationAssignmentStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientViolationAssignmentStatus.VALIDATED -> return next == LenientViolationAssignmentStatus.RETIRED
            LenientViolationAssignmentStatus.PUBLISHED -> {
                when (next) {
                    LenientViolationAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientViolationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient violation assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lenient violation assignment"} $code"

    fun effectiveExposure(): Double = round((priority.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): LenientViolationAssignment {
        return LenientViolationAssignment(
            code = code,
            label = label,
            createdAt = createdAt,
            sequence = sequence,
            distanceKm = distanceKm,
            discount = discount,
            score = score,
            priority = priority,
            pallets = pallets,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient violation assignment in status created. */
fun newLenientViolationAssignment(code: String, label: String): LenientViolationAssignment {
    return LenientViolationAssignment(
        code = code,
        label = label,
        createdAt = 0,
        sequence = 0,
        distanceKm = 0.0,
        discount = 0.0,
        score = 0.0,
        priority = 0,
        pallets = 0,
        windowDays = 0,
        status = LenientViolationAssignmentStatus.CREATED,
    )
}
