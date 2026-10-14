package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves trailer segments through their life cycle and keeps a journal of what it did. */
data class TrailerSegmentManager(
    var store: TrailerSegmentStore,
    var validator: TrailerSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("trailer segment $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("trailer segment $code not found")
        val result = engine.evaluate(item)
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

fun newTrailerSegmentManager(store: TrailerSegmentStore, clock: Clock, flags: Registry): TrailerSegmentManager {
    return TrailerSegmentManager(
        store = store,
        validator = defaultTrailerSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerSegmentEngine(flags),
    )
}
