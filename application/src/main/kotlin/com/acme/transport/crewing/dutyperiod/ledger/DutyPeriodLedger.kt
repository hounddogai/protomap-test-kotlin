package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.kit.text.normalize

// The duty period ledger records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period ledger is in its life cycle. */
enum class DutyPeriodLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One duty period ledger, with what the planners need to act on it. */
data class DutyPeriodLedger(
    var code: String,
    var name: String,
    var country: String,
    var vehicleId: String,
    var loadKg: Double,
    var phone: String,
    var title: String,
    var status: DutyPeriodLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodLedgerStatus): Boolean {
        when (status) {
            DutyPeriodLedgerStatus.DRAFT -> return next == DutyPeriodLedgerStatus.PENDING || next == DutyPeriodLedgerStatus.CLOSED
            DutyPeriodLedgerStatus.PENDING -> return next == DutyPeriodLedgerStatus.ACTIVE || next == DutyPeriodLedgerStatus.CLOSED
            DutyPeriodLedgerStatus.ACTIVE -> return next == DutyPeriodLedgerStatus.CLOSED
            DutyPeriodLedgerStatus.SUSPENDED -> {
                when (next) {
                    DutyPeriodLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodLedgerStatus) {
        if (!canMoveTo(next)) {
            error("duty period ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = loadKg

    fun snapshot(): DutyPeriodLedger {
        return DutyPeriodLedger(
            code = code,
            name = name,
            country = country,
            vehicleId = vehicleId,
            loadKg = loadKg,
            phone = phone,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a duty period ledger in status draft. */
fun newDutyPeriodLedger(code: String, name: String): DutyPeriodLedger {
    return DutyPeriodLedger(
        code = code,
        name = name,
        country = "",
        vehicleId = "",
        loadKg = 0.0,
        phone = "",
        title = "",
        status = DutyPeriodLedgerStatus.DRAFT,
    )
}
