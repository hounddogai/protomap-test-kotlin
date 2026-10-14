package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered availability ledgers through their life cycle and keeps a journal of what it did. */
data class TieredAvailabilityLedgerDesk(
    var store: TieredAvailabilityLedgerStore,
    var validator: TieredAvailabilityLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredAvailabilityLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered availability ledger $code not found")
        validator.requireValid(item)
        item.moveTo(TieredAvailabilityLedgerStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered availability ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredAvailabilityLedgerStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered availability ledger $code not found")
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

fun newTieredAvailabilityLedgerDesk(store: TieredAvailabilityLedgerStore, clock: Clock, flags: Registry): TieredAvailabilityLedgerDesk {
    return TieredAvailabilityLedgerDesk(
        store = store,
        validator = defaultTieredAvailabilityLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredAvailabilityLedgerEngine(flags),
    )
}
