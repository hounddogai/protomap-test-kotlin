package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection policy is in its life cycle. */
enum class FallbackInspectionPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback inspection policy as the inspection team keeps it. */
data class FallbackInspectionPolicy(
    var code: String,
    var name: String,
    var sequence: Int,
    var channel: String,
    var maximum: Double,
    var status: FallbackInspectionPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionPolicyStatus): Boolean {
        when (status) {
            FallbackInspectionPolicyStatus.CREATED -> return next == FallbackInspectionPolicyStatus.VALIDATED || next == FallbackInspectionPolicyStatus.RETIRED
            FallbackInspectionPolicyStatus.VALIDATED -> {
                when (next) {
                    FallbackInspectionPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackInspectionPolicyStatus.PUBLISHED -> return next == FallbackInspectionPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackInspectionPolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((maximum + sequence.toDouble()) * 100) / 100

    fun snapshot(): FallbackInspectionPolicy {
        return FallbackInspectionPolicy(
            code = code,
            name = name,
            sequence = sequence,
            channel = channel,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback inspection policy in status created. */
fun newFallbackInspectionPolicy(code: String, name: String): FallbackInspectionPolicy {
    return FallbackInspectionPolicy(
        code = code,
        name = name,
        sequence = 0,
        channel = "",
        maximum = 0.0,
        status = FallbackInspectionPolicyStatus.CREATED,
    )
}
