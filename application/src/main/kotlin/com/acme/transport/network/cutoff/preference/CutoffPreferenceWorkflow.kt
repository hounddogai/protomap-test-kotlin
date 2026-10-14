package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves cutoff preferences through their life cycle and keeps a journal of what it did. */
data class CutoffPreferenceWorkflow(
    var store: CutoffPreferenceStore,
    var validator: CutoffPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("cutoff preference $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffPreferenceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffPreferenceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("cutoff preference $code not found")
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

fun newCutoffPreferenceWorkflow(store: CutoffPreferenceStore, clock: Clock, flags: Registry): CutoffPreferenceWorkflow {
    return CutoffPreferenceWorkflow(
        store = store,
        validator = defaultCutoffPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffPreferenceEngine(flags),
    )
}
