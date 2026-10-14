package com.acme.transport.linehaul.trailer.template

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer thresholds through their life cycle and keeps a journal of what it did. */
data class TrailerThresholdWorkflow(
    var store: TrailerThresholdStore,
    var validator: TrailerThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer threshold $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("trailer threshold $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newTrailerThresholdWorkflow(store: TrailerThresholdStore, clock: Clock): TrailerThresholdWorkflow {
    return TrailerThresholdWorkflow(
        store = store,
        validator = defaultTrailerThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerThresholdEngine(),
    )
}
