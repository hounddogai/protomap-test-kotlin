package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves manual coaching ledgers through their life cycle and keeps a journal of what it did. */
data class ManualCoachingLedgerManager(
    var store: ManualCoachingLedgerStore,
    var validator: ManualCoachingLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualCoachingLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual coaching ledger $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCoachingLedgerStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual coaching ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCoachingLedgerStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual coaching ledger $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newManualCoachingLedgerManager(store: ManualCoachingLedgerStore, clock: Clock, flags: Registry): ManualCoachingLedgerManager {
    return ManualCoachingLedgerManager(
        store = store,
        validator = defaultManualCoachingLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualCoachingLedgerEngine(flags),
    )
}
