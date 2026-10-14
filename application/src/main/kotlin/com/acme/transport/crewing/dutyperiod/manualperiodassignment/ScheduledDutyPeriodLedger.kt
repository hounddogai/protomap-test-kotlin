package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled duty period ledger is in its life cycle. */
enum class ScheduledDutyPeriodLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled duty period ledger, with what the planners need to act on it. */
data class ScheduledDutyPeriodLedger(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var maximum: Double,
    var depot: String,
    var region: String,
    var channel: String,
    var score: Double,
    var status: ScheduledDutyPeriodLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDutyPeriodLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDutyPeriodLedgerStatus): Boolean {
        when (status) {
            ScheduledDutyPeriodLedgerStatus.CREATED -> return next == ScheduledDutyPeriodLedgerStatus.VALIDATED || next == ScheduledDutyPeriodLedgerStatus.RETIRED
            ScheduledDutyPeriodLedgerStatus.VALIDATED -> {
                when (next) {
                    ScheduledDutyPeriodLedgerStatus.PUBLISHED, ScheduledDutyPeriodLedgerStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledDutyPeriodLedgerStatus.PUBLISHED -> {
                when (next) {
                    ScheduledDutyPeriodLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDutyPeriodLedgerStatus) {
        if (!canMoveTo(next)) {
            error("scheduled duty period ledger $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled duty period ledger"} $code"

    fun adjustedLoad(): Double = round((maximum + score) * 100) / 100

    fun snapshot(): ScheduledDutyPeriodLedger {
        return ScheduledDutyPeriodLedger(
            code = code,
            name = name,
            updatedAt = updatedAt,
            maximum = maximum,
            depot = depot,
            region = region,
            channel = channel,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled duty period ledger in status created. */
fun newScheduledDutyPeriodLedger(code: String, name: String): ScheduledDutyPeriodLedger {
    return ScheduledDutyPeriodLedger(
        code = code,
        name = name,
        updatedAt = 0,
        maximum = 0.0,
        depot = "",
        region = "",
        channel = "",
        score = 0.0,
        status = ScheduledDutyPeriodLedgerStatus.CREATED,
    )
}
