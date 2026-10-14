package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves rolling trailer yard windows through their life cycle and keeps a journal of what it did. */
data class RollingTrailerYardWindowService(
    var store: RollingTrailerYardWindowStore,
    var validator: RollingTrailerYardWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingTrailerYardWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling trailer yard window $code not found")
        validator.requireValid(item)
        item.moveTo(RollingTrailerYardWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling trailer yard window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingTrailerYardWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling trailer yard window $code not found")
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

fun newRollingTrailerYardWindowService(store: RollingTrailerYardWindowStore, clock: Clock, flags: Registry): RollingTrailerYardWindowService {
    return RollingTrailerYardWindowService(
        store = store,
        validator = defaultRollingTrailerYardWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingTrailerYardWindowEngine(flags),
    )
}
