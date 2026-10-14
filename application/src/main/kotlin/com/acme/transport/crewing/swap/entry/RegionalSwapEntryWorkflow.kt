package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional swap entries through their life cycle and keeps a journal of what it did. */
data class RegionalSwapEntryCoordinator(
    var store: RegionalSwapEntryStore,
    var validator: RegionalSwapEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalSwapEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional swap entry $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalSwapEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional swap entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalSwapEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional swap entry $code not found")
        val result = engine.price(item)
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

fun newRegionalSwapEntryCoordinator(store: RegionalSwapEntryStore, clock: Clock, flags: Registry): RegionalSwapEntryCoordinator {
    return RegionalSwapEntryCoordinator(
        store = store,
        validator = defaultRegionalSwapEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalSwapEntryEngine(flags),
    )
}
