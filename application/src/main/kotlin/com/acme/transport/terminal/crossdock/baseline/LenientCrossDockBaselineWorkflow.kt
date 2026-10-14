package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.terminal.clock.Clock

/** Moves lenient cross dock baselines through their life cycle and keeps a journal of what it did. */
data class LenientCrossDockBaselineManager(
    var store: LenientCrossDockBaselineStore,
    var validator: LenientCrossDockBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCrossDockBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient cross dock baseline $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCrossDockBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient cross dock baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCrossDockBaselineStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient cross dock baseline $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newLenientCrossDockBaselineManager(store: LenientCrossDockBaselineStore, clock: Clock): LenientCrossDockBaselineManager {
    return LenientCrossDockBaselineManager(
        store = store,
        validator = defaultLenientCrossDockBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCrossDockBaselineEngine(),
    )
}
