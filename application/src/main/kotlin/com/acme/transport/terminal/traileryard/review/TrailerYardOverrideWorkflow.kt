package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard overrides through their life cycle and keeps a journal of what it did. */
data class TrailerYardOverrideService(
    var store: TrailerYardOverrideStore,
    var validator: TrailerYardOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerYardOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("trailer yard override $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardOverrideStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardOverrideStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("trailer yard override $code not found")
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

fun newTrailerYardOverrideService(store: TrailerYardOverrideStore, clock: Clock, flags: Registry): TrailerYardOverrideService {
    return TrailerYardOverrideService(
        store = store,
        validator = defaultTrailerYardOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerYardOverrideEngine(flags),
    )
}
