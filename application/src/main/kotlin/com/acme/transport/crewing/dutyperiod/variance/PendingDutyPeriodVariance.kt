package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending duty period variance records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a pending duty period variance is in its life cycle. */
enum class PendingDutyPeriodVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending duty period variance, with what the planners need to act on it. */
data class PendingDutyPeriodVariance(
    var code: String,
    var title: String,
    var routeCode: String,
    var visible: Boolean,
    var currency: String,
    var score: Double,
    var ratio: Double,
    var batchSize: Int,
    var label: String,
    var locale: String,
    var description: String,
    var status: PendingDutyPeriodVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodVarianceStatus): Boolean {
        when (status) {
            PendingDutyPeriodVarianceStatus.CREATED -> return next == PendingDutyPeriodVarianceStatus.VALIDATED
            PendingDutyPeriodVarianceStatus.VALIDATED -> return next == PendingDutyPeriodVarianceStatus.PUBLISHED || next == PendingDutyPeriodVarianceStatus.RETIRED
            PendingDutyPeriodVarianceStatus.PUBLISHED -> {
                when (next) {
                    PendingDutyPeriodVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodVarianceStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending duty period variance"} $code"

    fun adjustedTotal(): Double = round(batchSize.toDouble() * score * 100) / 100

    fun snapshot(): PendingDutyPeriodVariance {
        return PendingDutyPeriodVariance(
            code = code,
            title = title,
            routeCode = routeCode,
            visible = visible,
            currency = currency,
            score = score,
            ratio = ratio,
            batchSize = batchSize,
            label = label,
            locale = locale,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending duty period variance in status created. */
fun newPendingDutyPeriodVariance(code: String, title: String): PendingDutyPeriodVariance {
    return PendingDutyPeriodVariance(
        code = code,
        title = title,
        routeCode = "",
        visible = false,
        currency = "",
        score = 0.0,
        ratio = 0.0,
        batchSize = 0,
        label = "",
        locale = "",
        description = "",
        status = PendingDutyPeriodVarianceStatus.CREATED,
    )
}
