package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient duty period variance is in its life cycle. */
enum class LenientDutyPeriodVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient duty period variance as the duty period team keeps it. */
data class LenientDutyPeriodVariance(
    var code: String,
    var title: String,
    var archived: Boolean,
    var locale: String,
    var maximum: Double,
    var margin: Double,
    var distanceKm: Double,
    var status: LenientDutyPeriodVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDutyPeriodVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDutyPeriodVarianceStatus): Boolean {
        when (status) {
            LenientDutyPeriodVarianceStatus.PROPOSED -> {
                when (next) {
                    LenientDutyPeriodVarianceStatus.APPROVED, LenientDutyPeriodVarianceStatus.REJECTED -> return true
                }
                return false
            }
            LenientDutyPeriodVarianceStatus.APPROVED -> {
                when (next) {
                    LenientDutyPeriodVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientDutyPeriodVarianceStatus.REJECTED -> return next == LenientDutyPeriodVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientDutyPeriodVarianceStatus) {
        if (!canMoveTo(next)) {
            error("lenient duty period variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient duty period variance"} $code"

    fun netVolume(): Double = round(distanceKm * margin * 100) / 100

    fun snapshot(): LenientDutyPeriodVariance {
        return LenientDutyPeriodVariance(
            code = code,
            title = title,
            archived = archived,
            locale = locale,
            maximum = maximum,
            margin = margin,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient duty period variance in status proposed. */
fun newLenientDutyPeriodVariance(code: String, title: String): LenientDutyPeriodVariance {
    return LenientDutyPeriodVariance(
        code = code,
        title = title,
        archived = false,
        locale = "",
        maximum = 0.0,
        margin = 0.0,
        distanceKm = 0.0,
        status = LenientDutyPeriodVarianceStatus.PROPOSED,
    )
}
