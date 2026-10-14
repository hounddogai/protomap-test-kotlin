package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal redelivery ledgers through their life cycle and keeps a journal of what it did. */
data class SeasonalRedeliveryLedgerDesk(
    var store: SeasonalRedeliveryLedgerStore,
    var validator: SeasonalRedeliveryLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalRedeliveryLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal redelivery ledger $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRedeliveryLedgerStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal redelivery ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRedeliveryLedgerStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal redelivery ledger $code not found")
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

fun newSeasonalRedeliveryLedgerDesk(store: SeasonalRedeliveryLedgerStore, clock: Clock, flags: Registry): SeasonalRedeliveryLedgerDesk {
    return SeasonalRedeliveryLedgerDesk(
        store = store,
        validator = defaultSeasonalRedeliveryLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalRedeliveryLedgerEngine(flags),
    )
}
