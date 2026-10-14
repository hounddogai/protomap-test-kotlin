package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan ledgers through their life cycle and keeps a journal of what it did. */
data class LanePlanLedgerManager(
    var store: LanePlanLedgerStore,
    var validator: LanePlanLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan ledger $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanLedgerStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanLedgerStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lane plan ledger $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newLanePlanLedgerManager(store: LanePlanLedgerStore, clock: Clock): LanePlanLedgerManager {
    return LanePlanLedgerManager(
        store = store,
        validator = defaultLanePlanLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanLedgerEngine(),
    )
}
