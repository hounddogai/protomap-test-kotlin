package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.clock.Clock

/** Moves fallback idling segments through their life cycle and keeps a journal of what it did. */
data class FallbackIdlingSegmentManager(
    var store: FallbackIdlingSegmentStore,
    var validator: FallbackIdlingSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackIdlingSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback idling segment $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackIdlingSegmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback idling segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackIdlingSegmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback idling segment $code not found")
        val result = engine.evaluate(item)
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

fun newFallbackIdlingSegmentManager(store: FallbackIdlingSegmentStore, clock: Clock): FallbackIdlingSegmentManager {
    return FallbackIdlingSegmentManager(
        store = store,
        validator = defaultFallbackIdlingSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackIdlingSegmentEngine(),
    )
}
