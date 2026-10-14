package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves default trailer yard windows through their life cycle and keeps a journal of what it did. */
data class DefaultTrailerYardWindowService(
    var store: DefaultTrailerYardWindowStore,
    var validator: DefaultTrailerYardWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultTrailerYardWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default trailer yard window $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTrailerYardWindowStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default trailer yard window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTrailerYardWindowStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default trailer yard window $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newDefaultTrailerYardWindowService(store: DefaultTrailerYardWindowStore, clock: Clock, flags: Registry): DefaultTrailerYardWindowService {
    return DefaultTrailerYardWindowService(
        store = store,
        validator = defaultDefaultTrailerYardWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultTrailerYardWindowEngine(flags),
    )
}
