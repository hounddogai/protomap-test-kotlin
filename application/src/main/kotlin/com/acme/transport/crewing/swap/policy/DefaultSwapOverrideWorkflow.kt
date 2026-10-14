package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default swap overrides through their life cycle and keeps a journal of what it did. */
data class DefaultSwapOverrideManager(
    var store: DefaultSwapOverrideStore,
    var validator: DefaultSwapOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultSwapOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default swap override $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultSwapOverrideStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default swap override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultSwapOverrideStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default swap override $code not found")
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

fun newDefaultSwapOverrideManager(store: DefaultSwapOverrideStore, clock: Clock, flags: Registry): DefaultSwapOverrideManager {
    return DefaultSwapOverrideManager(
        store = store,
        validator = defaultDefaultSwapOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultSwapOverrideEngine(flags),
    )
}
