package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves trailer cycles through their life cycle and keeps a journal of what it did. */
data class TrailerCycleManager(
    var store: TrailerCycleStore,
    var validator: TrailerCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer cycle $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("trailer cycle $code not found")
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

fun newTrailerCycleManager(store: TrailerCycleStore, clock: Clock, flags: Registry): TrailerCycleManager {
    return TrailerCycleManager(
        store = store,
        validator = defaultTrailerCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerCycleEngine(flags),
    )
}
