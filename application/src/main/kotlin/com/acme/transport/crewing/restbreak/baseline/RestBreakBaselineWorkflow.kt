package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break baselines through their life cycle and keeps a journal of what it did. */
data class RestBreakBaselineManager(
    var store: RestBreakBaselineStore,
    var validator: RestBreakBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rest break baseline $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakBaselineStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rest break baseline $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newRestBreakBaselineManager(store: RestBreakBaselineStore, clock: Clock, flags: Registry): RestBreakBaselineManager {
    return RestBreakBaselineManager(
        store = store,
        validator = defaultRestBreakBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakBaselineEngine(flags),
    )
}
