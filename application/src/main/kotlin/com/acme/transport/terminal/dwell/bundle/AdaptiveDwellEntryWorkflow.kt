package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves adaptive dwell entries through their life cycle and keeps a journal of what it did. */
data class AdaptiveDwellEntryManager(
    var store: AdaptiveDwellEntryStore,
    var validator: AdaptiveDwellEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDwellEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive dwell entry $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDwellEntryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive dwell entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDwellEntryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive dwell entry $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newAdaptiveDwellEntryManager(store: AdaptiveDwellEntryStore, clock: Clock, flags: Registry): AdaptiveDwellEntryManager {
    return AdaptiveDwellEntryManager(
        store = store,
        validator = defaultAdaptiveDwellEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDwellEntryEngine(flags),
    )
}
