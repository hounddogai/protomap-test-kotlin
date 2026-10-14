package com.acme.transport.crewing.roster.milestone

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending roster preferences through their life cycle and keeps a journal of what it did. */
data class PendingRosterPreferenceWorkflow(
    var store: PendingRosterPreferenceStore,
    var validator: PendingRosterPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingRosterPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending roster preference $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRosterPreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending roster preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRosterPreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending roster preference $code not found")
        val result = engine.price(item)
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

fun newPendingRosterPreferenceWorkflow(store: PendingRosterPreferenceStore, clock: Clock, flags: Registry): PendingRosterPreferenceWorkflow {
    return PendingRosterPreferenceWorkflow(
        store = store,
        validator = defaultPendingRosterPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingRosterPreferenceEngine(flags),
    )
}
