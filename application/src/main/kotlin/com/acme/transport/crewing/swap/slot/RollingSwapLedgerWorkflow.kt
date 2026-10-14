package com.acme.transport.crewing.swap.slot

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling swap ledgers through their life cycle and keeps a journal of what it did. */
data class RollingSwapLedgerWorkflow(
    var store: RollingSwapLedgerStore,
    var validator: RollingSwapLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingSwapLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling swap ledger $code not found")
        validator.requireValid(item)
        item.moveTo(RollingSwapLedgerStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling swap ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingSwapLedgerStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling swap ledger $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newRollingSwapLedgerWorkflow(store: RollingSwapLedgerStore, clock: Clock, flags: Registry): RollingSwapLedgerWorkflow {
    return RollingSwapLedgerWorkflow(
        store = store,
        validator = defaultRollingSwapLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingSwapLedgerEngine(flags),
    )
}
