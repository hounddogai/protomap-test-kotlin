package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dispute plan is in its life cycle. */
enum class FallbackDisputePlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fallback dispute plan and its current status. */
data class FallbackDisputePlan(
    var code: String,
    var label: String,
    var revision: Int,
    var windowDays: Int,
    var maximum: Double,
    var etaMinutes: Int,
    var title: String,
    var graceDays: Int,
    var attempts: Int,
    var externalRef: String,
    var status: FallbackDisputePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDisputePlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDisputePlanStatus): Boolean {
        when (status) {
            FallbackDisputePlanStatus.PLANNED -> return next == FallbackDisputePlanStatus.COMPLETED
            FallbackDisputePlanStatus.SCHEDULED -> return next == FallbackDisputePlanStatus.IN_PROGRESS || next == FallbackDisputePlanStatus.COMPLETED
            FallbackDisputePlanStatus.IN_PROGRESS -> return next == FallbackDisputePlanStatus.COMPLETED || next == FallbackDisputePlanStatus.CANCELLED
            FallbackDisputePlanStatus.COMPLETED -> return next == FallbackDisputePlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackDisputePlanStatus) {
        if (!canMoveTo(next)) {
            error("fallback dispute plan $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"fallback dispute plan"} $code"

    fun weightedExposure(): Double = round((graceDays.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackDisputePlan {
        return FallbackDisputePlan(
            code = code,
            label = label,
            revision = revision,
            windowDays = windowDays,
            maximum = maximum,
            etaMinutes = etaMinutes,
            title = title,
            graceDays = graceDays,
            attempts = attempts,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback dispute plan in status planned. */
fun newFallbackDisputePlan(code: String, label: String): FallbackDisputePlan {
    return FallbackDisputePlan(
        code = code,
        label = label,
        revision = 0,
        windowDays = 0,
        maximum = 0.0,
        etaMinutes = 0,
        title = "",
        graceDays = 0,
        attempts = 0,
        externalRef = "",
        status = FallbackDisputePlanStatus.PLANNED,
    )
}
