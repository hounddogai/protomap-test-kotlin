package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tender assignment is in its life cycle. */
enum class FallbackTenderAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback tender assignment and its current status. */
data class FallbackTenderAssignment(
    var code: String,
    var label: String,
    var limit: Int,
    var cost: Double,
    var rate: Double,
    var status: FallbackTenderAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTenderAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTenderAssignmentStatus): Boolean {
        when (status) {
            FallbackTenderAssignmentStatus.CREATED -> return next == FallbackTenderAssignmentStatus.PUBLISHED || next == FallbackTenderAssignmentStatus.RETIRED
            FallbackTenderAssignmentStatus.VALIDATED -> {
                when (next) {
                    FallbackTenderAssignmentStatus.PUBLISHED, FallbackTenderAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            FallbackTenderAssignmentStatus.PUBLISHED -> return next == FallbackTenderAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackTenderAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback tender assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round(cost * rate * 100) / 100

    fun snapshot(): FallbackTenderAssignment {
        return FallbackTenderAssignment(
            code = code,
            label = label,
            limit = limit,
            cost = cost,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback tender assignment in status created. */
fun newFallbackTenderAssignment(code: String, label: String): FallbackTenderAssignment {
    return FallbackTenderAssignment(
        code = code,
        label = label,
        limit = 0,
        cost = 0.0,
        rate = 0.0,
        status = FallbackTenderAssignmentStatus.CREATED,
    )
}
