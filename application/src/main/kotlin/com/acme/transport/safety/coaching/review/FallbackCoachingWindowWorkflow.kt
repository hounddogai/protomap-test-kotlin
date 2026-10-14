package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves fallback coaching windows through their life cycle and keeps a journal of what it did. */
data class FallbackCoachingWindowService(
    var store: FallbackCoachingWindowStore,
    var validator: FallbackCoachingWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackCoachingWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback coaching window $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCoachingWindowStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback coaching window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCoachingWindowStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback coaching window $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newFallbackCoachingWindowService(store: FallbackCoachingWindowStore, clock: Clock, flags: Registry): FallbackCoachingWindowService {
    return FallbackCoachingWindowService(
        store = store,
        validator = defaultFallbackCoachingWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackCoachingWindowEngine(flags),
    )
}
