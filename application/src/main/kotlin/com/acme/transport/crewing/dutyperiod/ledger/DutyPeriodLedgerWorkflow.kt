package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.crewing.clock.Clock

/** Moves duty period ledgers through their life cycle and keeps a journal of what it did. */
data class DutyPeriodLedgerDesk(
    var store: DutyPeriodLedgerStore,
    var validator: DutyPeriodLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("duty period ledger $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodLedgerStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodLedgerStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newDutyPeriodLedgerDesk(store: DutyPeriodLedgerStore, clock: Clock): DutyPeriodLedgerDesk {
    return DutyPeriodLedgerDesk(
        store = store,
        validator = defaultDutyPeriodLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
