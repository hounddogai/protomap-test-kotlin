package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard windows through their life cycle and keeps a journal of what it did. */
data class TrailerYardWindowService(
    var store: TrailerYardWindowStore,
    var validator: TrailerYardWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerYardWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer yard window $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardWindowStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardWindowStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("trailer yard window $code not found")
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

fun newTrailerYardWindowService(store: TrailerYardWindowStore, clock: Clock): TrailerYardWindowService {
    return TrailerYardWindowService(
        store = store,
        validator = defaultTrailerYardWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerYardWindowEngine(),
    )
}
