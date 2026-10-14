package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard thresholds through their life cycle and keeps a journal of what it did. */
data class TrailerYardThresholdCoordinator(
    var store: TrailerYardThresholdStore,
    var validator: TrailerYardThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerYardThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer yard threshold $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardThresholdStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardThresholdStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("trailer yard threshold $code not found")
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

fun newTrailerYardThresholdCoordinator(store: TrailerYardThresholdStore, clock: Clock, flags: Registry): TrailerYardThresholdCoordinator {
    return TrailerYardThresholdCoordinator(
        store = store,
        validator = defaultTrailerYardThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerYardThresholdEngine(flags),
    )
}
