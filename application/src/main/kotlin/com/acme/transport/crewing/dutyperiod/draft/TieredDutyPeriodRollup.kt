package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered duty period rollup is in its life cycle. */
enum class TieredDutyPeriodRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered duty period rollup and its current status. */
data class TieredDutyPeriodRollup(
    var code: String,
    var title: String,
    var threshold: Double,
    var retries: Int,
    var approved: Boolean,
    var description: String,
    var status: TieredDutyPeriodRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDutyPeriodRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDutyPeriodRollupStatus): Boolean {
        when (status) {
            TieredDutyPeriodRollupStatus.IDLE -> return next == TieredDutyPeriodRollupStatus.READY || next == TieredDutyPeriodRollupStatus.DRAINING
            TieredDutyPeriodRollupStatus.WARMING -> return next == TieredDutyPeriodRollupStatus.STOPPED
            TieredDutyPeriodRollupStatus.READY -> {
                when (next) {
                    TieredDutyPeriodRollupStatus.DRAINING, TieredDutyPeriodRollupStatus.STOPPED -> return true
                }
                return false
            }
            TieredDutyPeriodRollupStatus.DRAINING -> return next == TieredDutyPeriodRollupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredDutyPeriodRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered duty period rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round((threshold + retries.toDouble()) * 100) / 100

    fun snapshot(): TieredDutyPeriodRollup {
        return TieredDutyPeriodRollup(
            code = code,
            title = title,
            threshold = threshold,
            retries = retries,
            approved = approved,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered duty period rollup in status idle. */
fun newTieredDutyPeriodRollup(code: String, title: String): TieredDutyPeriodRollup {
    return TieredDutyPeriodRollup(
        code = code,
        title = title,
        threshold = 0.0,
        retries = 0,
        approved = false,
        description = "",
        status = TieredDutyPeriodRollupStatus.IDLE,
    )
}
