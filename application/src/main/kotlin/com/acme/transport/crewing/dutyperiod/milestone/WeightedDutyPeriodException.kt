package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted duty period exception is in its life cycle. */
enum class WeightedDutyPeriodExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted duty period exception and its current status. */
data class WeightedDutyPeriodException(
    var code: String,
    var name: String,
    var source: String,
    var contactEmail: String,
    var loadKg: Double,
    var rate: Double,
    var capacity: Int,
    var status: WeightedDutyPeriodExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDutyPeriodExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDutyPeriodExceptionStatus): Boolean {
        when (status) {
            WeightedDutyPeriodExceptionStatus.PLANNED -> return next == WeightedDutyPeriodExceptionStatus.IN_PROGRESS || next == WeightedDutyPeriodExceptionStatus.CANCELLED
            WeightedDutyPeriodExceptionStatus.SCHEDULED -> return next == WeightedDutyPeriodExceptionStatus.IN_PROGRESS
            WeightedDutyPeriodExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedDutyPeriodExceptionStatus.COMPLETED, WeightedDutyPeriodExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedDutyPeriodExceptionStatus.COMPLETED -> return next == WeightedDutyPeriodExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedDutyPeriodExceptionStatus) {
        if (!canMoveTo(next)) {
            error("weighted duty period exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((loadKg + rate) * 100) / 100

    fun snapshot(): WeightedDutyPeriodException {
        return WeightedDutyPeriodException(
            code = code,
            name = name,
            source = source,
            contactEmail = contactEmail,
            loadKg = loadKg,
            rate = rate,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted duty period exception in status planned. */
fun newWeightedDutyPeriodException(code: String, name: String): WeightedDutyPeriodException {
    return WeightedDutyPeriodException(
        code = code,
        name = name,
        source = "",
        contactEmail = "",
        loadKg = 0.0,
        rate = 0.0,
        capacity = 0,
        status = WeightedDutyPeriodExceptionStatus.PLANNED,
    )
}
