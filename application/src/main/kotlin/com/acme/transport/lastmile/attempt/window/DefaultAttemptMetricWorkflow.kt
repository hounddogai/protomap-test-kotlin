package com.acme.transport.lastmile.attempt.window

import com.acme.transport.lastmile.clock.Clock

/** Moves default attempt metrics through their life cycle and keeps a journal of what it did. */
data class DefaultAttemptMetricCoordinator(
    var store: DefaultAttemptMetricStore,
    var validator: DefaultAttemptMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultAttemptMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default attempt metric $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAttemptMetricStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default attempt metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAttemptMetricStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default attempt metric $code not found")
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

fun newDefaultAttemptMetricCoordinator(store: DefaultAttemptMetricStore, clock: Clock): DefaultAttemptMetricCoordinator {
    return DefaultAttemptMetricCoordinator(
        store = store,
        validator = defaultDefaultAttemptMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultAttemptMetricEngine(),
    )
}
