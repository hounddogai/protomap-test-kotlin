package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.text.normalize

/** Where a fallback utilization target is in its life cycle. */
enum class FallbackUtilizationTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a fallback utilization target and its current status. */
data class FallbackUtilizationTarget(
    var code: String,
    var label: String,
    var timezone: String,
    var notes: String,
    var quantity: Int,
    var status: FallbackUtilizationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackUtilizationTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackUtilizationTargetStatus): Boolean {
        when (status) {
            FallbackUtilizationTargetStatus.PROPOSED -> {
                when (next) {
                    FallbackUtilizationTargetStatus.APPROVED, FallbackUtilizationTargetStatus.REJECTED -> return true
                }
                return false
            }
            FallbackUtilizationTargetStatus.APPROVED -> return next == FallbackUtilizationTargetStatus.WITHDRAWN
            FallbackUtilizationTargetStatus.REJECTED -> return next == FallbackUtilizationTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackUtilizationTargetStatus) {
        if (!canMoveTo(next)) {
            error("fallback utilization target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback utilization target"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): FallbackUtilizationTarget {
        return FallbackUtilizationTarget(
            code = code,
            label = label,
            timezone = timezone,
            notes = notes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback utilization target in status proposed. */
fun newFallbackUtilizationTarget(code: String, label: String): FallbackUtilizationTarget {
    return FallbackUtilizationTarget(
        code = code,
        label = label,
        timezone = "",
        notes = "",
        quantity = 0,
        status = FallbackUtilizationTargetStatus.PROPOSED,
    )
}
