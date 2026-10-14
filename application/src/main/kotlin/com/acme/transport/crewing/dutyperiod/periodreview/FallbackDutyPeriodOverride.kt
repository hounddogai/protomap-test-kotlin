package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback duty period override is in its life cycle. */
enum class FallbackDutyPeriodOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback duty period override. Its code is unique within a tenant. */
data class FallbackDutyPeriodOverride(
    var code: String,
    var title: String,
    var dock: String,
    var ceiling: Double,
    var etaMinutes: Int,
    var summary: String,
    var score: Double,
    var status: FallbackDutyPeriodOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDutyPeriodOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDutyPeriodOverrideStatus): Boolean {
        when (status) {
            FallbackDutyPeriodOverrideStatus.PROPOSED -> return next == FallbackDutyPeriodOverrideStatus.REJECTED
            FallbackDutyPeriodOverrideStatus.APPROVED -> {
                when (next) {
                    FallbackDutyPeriodOverrideStatus.REJECTED, FallbackDutyPeriodOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackDutyPeriodOverrideStatus.REJECTED -> return next == FallbackDutyPeriodOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackDutyPeriodOverrideStatus) {
        if (!canMoveTo(next)) {
            error("fallback duty period override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback duty period override"} $code"

    fun projectedValue(): Double = round((etaMinutes.toDouble() - ceiling) * 100) / 100

    fun snapshot(): FallbackDutyPeriodOverride {
        return FallbackDutyPeriodOverride(
            code = code,
            title = title,
            dock = dock,
            ceiling = ceiling,
            etaMinutes = etaMinutes,
            summary = summary,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback duty period override in status proposed. */
fun newFallbackDutyPeriodOverride(code: String, title: String): FallbackDutyPeriodOverride {
    return FallbackDutyPeriodOverride(
        code = code,
        title = title,
        dock = "",
        ceiling = 0.0,
        etaMinutes = 0,
        summary = "",
        score = 0.0,
        status = FallbackDutyPeriodOverrideStatus.PROPOSED,
    )
}
