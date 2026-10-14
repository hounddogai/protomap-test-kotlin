package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback rate card assignment is in its life cycle. */
enum class FallbackRateCardAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback rate card assignment. Its code is unique within a tenant. */
data class FallbackRateCardAssignment(
    var code: String,
    var name: String,
    var carrier: String,
    var odometer: Int,
    var batchSize: Int,
    var description: String,
    var status: FallbackRateCardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRateCardAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRateCardAssignmentStatus): Boolean {
        when (status) {
            FallbackRateCardAssignmentStatus.CREATED -> return next == FallbackRateCardAssignmentStatus.VALIDATED || next == FallbackRateCardAssignmentStatus.PUBLISHED
            FallbackRateCardAssignmentStatus.VALIDATED -> {
                when (next) {
                    FallbackRateCardAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            FallbackRateCardAssignmentStatus.PUBLISHED -> {
                when (next) {
                    FallbackRateCardAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRateCardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback rate card assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((odometer.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): FallbackRateCardAssignment {
        return FallbackRateCardAssignment(
            code = code,
            name = name,
            carrier = carrier,
            odometer = odometer,
            batchSize = batchSize,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback rate card assignment in status created. */
fun newFallbackRateCardAssignment(code: String, name: String): FallbackRateCardAssignment {
    return FallbackRateCardAssignment(
        code = code,
        name = name,
        carrier = "",
        odometer = 0,
        batchSize = 0,
        description = "",
        status = FallbackRateCardAssignmentStatus.CREATED,
    )
}
