package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves primary drop entries through their life cycle and keeps a journal of what it did. */
data class PrimaryDropEntryCoordinator(
    var store: PrimaryDropEntryStore,
    var validator: PrimaryDropEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDropEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary drop entry $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDropEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary drop entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDropEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary drop entry $code not found")
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

fun newPrimaryDropEntryCoordinator(store: PrimaryDropEntryStore, clock: Clock, flags: Registry): PrimaryDropEntryCoordinator {
    return PrimaryDropEntryCoordinator(
        store = store,
        validator = defaultPrimaryDropEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDropEntryEngine(flags),
    )
}
