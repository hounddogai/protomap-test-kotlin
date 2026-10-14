package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard plans through their life cycle and keeps a journal of what it did. */
data class TrailerYardPlanManager(
    var store: TrailerYardPlanStore,
    var validator: TrailerYardPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerYardPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer yard plan $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardPlanStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardPlanStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("trailer yard plan $code not found")
        val result = engine.evaluate(item)
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

fun newTrailerYardPlanManager(store: TrailerYardPlanStore, clock: Clock): TrailerYardPlanManager {
    return TrailerYardPlanManager(
        store = store,
        validator = defaultTrailerYardPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerYardPlanEngine(),
    )
}
