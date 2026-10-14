package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy duty period ledger is in its life cycle. */
enum class LegacyDutyPeriodLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy duty period ledger. Its code is unique within a tenant. */
data class LegacyDutyPeriodLedger(
    var code: String,
    var name: String,
    var quantity: Int,
    var externalRef: String,
    var endsAt: Int,
    var summary: String,
    var status: LegacyDutyPeriodLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDutyPeriodLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDutyPeriodLedgerStatus): Boolean {
        when (status) {
            LegacyDutyPeriodLedgerStatus.IDLE -> {
                when (next) {
                    LegacyDutyPeriodLedgerStatus.WARMING, LegacyDutyPeriodLedgerStatus.STOPPED -> return true
                }
                return false
            }
            LegacyDutyPeriodLedgerStatus.WARMING -> return next == LegacyDutyPeriodLedgerStatus.READY || next == LegacyDutyPeriodLedgerStatus.DRAINING
            LegacyDutyPeriodLedgerStatus.READY -> return next == LegacyDutyPeriodLedgerStatus.STOPPED
            LegacyDutyPeriodLedgerStatus.DRAINING -> return next == LegacyDutyPeriodLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyDutyPeriodLedgerStatus) {
        if (!canMoveTo(next)) {
            error("legacy duty period ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((quantity.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyDutyPeriodLedger {
        return LegacyDutyPeriodLedger(
            code = code,
            name = name,
            quantity = quantity,
            externalRef = externalRef,
            endsAt = endsAt,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy duty period ledger in status idle. */
fun newLegacyDutyPeriodLedger(code: String, name: String): LegacyDutyPeriodLedger {
    return LegacyDutyPeriodLedger(
        code = code,
        name = name,
        quantity = 0,
        externalRef = "",
        endsAt = 0,
        summary = "",
        status = LegacyDutyPeriodLedgerStatus.IDLE,
    )
}
