package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves seasonal hub entries through their life cycle and keeps a journal of what it did. */
data class SeasonalHubEntryService(
    var store: SeasonalHubEntryStore,
    var validator: SeasonalHubEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalHubEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal hub entry $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalHubEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal hub entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalHubEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal hub entry $code not found")
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

fun newSeasonalHubEntryService(store: SeasonalHubEntryStore, clock: Clock, flags: Registry): SeasonalHubEntryService {
    return SeasonalHubEntryService(
        store = store,
        validator = defaultSeasonalHubEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalHubEntryEngine(flags),
    )
}
