package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback rest break cycles through their life cycle and keeps a journal of what it did. */
data class FallbackRestBreakCycleManager(
    var store: FallbackRestBreakCycleStore,
    var validator: FallbackRestBreakCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRestBreakCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback rest break cycle $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRestBreakCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback rest break cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRestBreakCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback rest break cycle $code not found")
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

fun newFallbackRestBreakCycleManager(store: FallbackRestBreakCycleStore, clock: Clock, flags: Registry): FallbackRestBreakCycleManager {
    return FallbackRestBreakCycleManager(
        store = store,
        validator = defaultFallbackRestBreakCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRestBreakCycleEngine(flags),
    )
}
