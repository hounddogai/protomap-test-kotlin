package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves capped near miss ledgers through their life cycle and keeps a journal of what it did. */
data class CappedNearMissLedgerWorkflow(
    var store: CappedNearMissLedgerStore,
    var validator: CappedNearMissLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedNearMissLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped near miss ledger $code not found")
        validator.requireValid(item)
        item.moveTo(CappedNearMissLedgerStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped near miss ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedNearMissLedgerStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped near miss ledger $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newCappedNearMissLedgerWorkflow(store: CappedNearMissLedgerStore, clock: Clock, flags: Registry): CappedNearMissLedgerWorkflow {
    return CappedNearMissLedgerWorkflow(
        store = store,
        validator = defaultCappedNearMissLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedNearMissLedgerEngine(flags),
    )
}
