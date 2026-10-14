package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period batch is in its life cycle. */
enum class DutyPeriodBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One duty period batch, with what the planners need to act on it. */
data class DutyPeriodBatch(
    var code: String,
    var title: String,
    var cost: Double,
    var minimum: Double,
    var reviewer: String,
    var checksum: String,
    var odometer: Int,
    var locale: String,
    var status: DutyPeriodBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodBatchStatus): Boolean {
        when (status) {
            DutyPeriodBatchStatus.REQUESTED -> return next == DutyPeriodBatchStatus.GRANTED || next == DutyPeriodBatchStatus.REVOKED
            DutyPeriodBatchStatus.GRANTED -> return next == DutyPeriodBatchStatus.REVOKED
            DutyPeriodBatchStatus.REVOKED -> {
                when (next) {
                    DutyPeriodBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodBatchStatus) {
        if (!canMoveTo(next)) {
            error("duty period batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((odometer.toDouble() - cost) * 100) / 100

    fun snapshot(): DutyPeriodBatch {
        return DutyPeriodBatch(
            code = code,
            title = title,
            cost = cost,
            minimum = minimum,
            reviewer = reviewer,
            checksum = checksum,
            odometer = odometer,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a duty period batch in status requested. */
fun newDutyPeriodBatch(code: String, title: String): DutyPeriodBatch {
    return DutyPeriodBatch(
        code = code,
        title = title,
        cost = 0.0,
        minimum = 0.0,
        reviewer = "",
        checksum = "",
        odometer = 0,
        locale = "",
        status = DutyPeriodBatchStatus.REQUESTED,
    )
}
