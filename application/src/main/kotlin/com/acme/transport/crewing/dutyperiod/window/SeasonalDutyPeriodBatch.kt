package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal duty period batch is in its life cycle. */
enum class SeasonalDutyPeriodBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal duty period batch. Its code is unique within a tenant. */
data class SeasonalDutyPeriodBatch(
    var code: String,
    var label: String,
    var description: String,
    var quantity: Int,
    var budget: Double,
    var minimum: Double,
    var capacity: Int,
    var depot: String,
    var status: SeasonalDutyPeriodBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDutyPeriodBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDutyPeriodBatchStatus): Boolean {
        when (status) {
            SeasonalDutyPeriodBatchStatus.REQUESTED -> return next == SeasonalDutyPeriodBatchStatus.REVOKED || next == SeasonalDutyPeriodBatchStatus.EXPIRED
            SeasonalDutyPeriodBatchStatus.GRANTED -> return next == SeasonalDutyPeriodBatchStatus.REVOKED || next == SeasonalDutyPeriodBatchStatus.EXPIRED
            SeasonalDutyPeriodBatchStatus.REVOKED -> {
                when (next) {
                    SeasonalDutyPeriodBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDutyPeriodBatchStatus) {
        if (!canMoveTo(next)) {
            error("seasonal duty period batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal duty period batch"} $code"

    fun netLoad(): Double = round((budget - quantity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDutyPeriodBatch {
        return SeasonalDutyPeriodBatch(
            code = code,
            label = label,
            description = description,
            quantity = quantity,
            budget = budget,
            minimum = minimum,
            capacity = capacity,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal duty period batch in status requested. */
fun newSeasonalDutyPeriodBatch(code: String, label: String): SeasonalDutyPeriodBatch {
    return SeasonalDutyPeriodBatch(
        code = code,
        label = label,
        description = "",
        quantity = 0,
        budget = 0.0,
        minimum = 0.0,
        capacity = 0,
        depot = "",
        status = SeasonalDutyPeriodBatchStatus.REQUESTED,
    )
}
