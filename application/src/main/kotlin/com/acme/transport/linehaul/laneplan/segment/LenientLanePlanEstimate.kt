package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient lane plan estimate is in its life cycle. */
enum class LenientLanePlanEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient lane plan estimate as the lane plan team keeps it. */
data class LenientLanePlanEstimate(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var budget: Double,
    var rate: Double,
    var status: LenientLanePlanEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLanePlanEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLanePlanEstimateStatus): Boolean {
        when (status) {
            LenientLanePlanEstimateStatus.DRAFT -> {
                when (next) {
                    LenientLanePlanEstimateStatus.ACTIVE, LenientLanePlanEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientLanePlanEstimateStatus.PENDING -> {
                when (next) {
                    LenientLanePlanEstimateStatus.ACTIVE -> return true
                }
                return false
            }
            LenientLanePlanEstimateStatus.ACTIVE -> return next == LenientLanePlanEstimateStatus.SUSPENDED || next == LenientLanePlanEstimateStatus.CLOSED
            LenientLanePlanEstimateStatus.SUSPENDED -> {
                when (next) {
                    LenientLanePlanEstimateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLanePlanEstimateStatus) {
        if (!canMoveTo(next)) {
            error("lenient lane plan estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient lane plan estimate"} $code"

    fun weightedExposure(): Double = round((rate + budget) * 100) / 100

    fun snapshot(): LenientLanePlanEstimate {
        return LenientLanePlanEstimate(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            budget = budget,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient lane plan estimate in status draft. */
fun newLenientLanePlanEstimate(code: String, title: String): LenientLanePlanEstimate {
    return LenientLanePlanEstimate(
        code = code,
        title = title,
        durationMinutes = 0,
        budget = 0.0,
        rate = 0.0,
        status = LenientLanePlanEstimateStatus.DRAFT,
    )
}
