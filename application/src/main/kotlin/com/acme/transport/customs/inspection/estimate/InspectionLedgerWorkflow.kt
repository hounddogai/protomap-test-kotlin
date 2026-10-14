package com.acme.transport.customs.inspection.estimate

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection ledgers through their life cycle and keeps a journal of what it did. */
data class InspectionLedgerManager(
    var store: InspectionLedgerStore,
    var validator: InspectionLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection ledger $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionLedgerStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionLedgerStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("inspection ledger $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newInspectionLedgerManager(store: InspectionLedgerStore, clock: Clock, flags: Registry): InspectionLedgerManager {
    return InspectionLedgerManager(
        store = store,
        validator = defaultInspectionLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionLedgerEngine(flags),
    )
}
