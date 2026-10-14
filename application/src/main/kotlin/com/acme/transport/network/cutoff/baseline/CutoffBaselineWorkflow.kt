package com.acme.transport.network.cutoff.baseline

import com.acme.transport.network.clock.Clock

/** Moves cutoff baselines through their life cycle and keeps a journal of what it did. */
data class CutoffBaselineManager(
    var store: CutoffBaselineStore,
    var validator: CutoffBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("cutoff baseline $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffBaselineStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCutoffBaselineManager(store: CutoffBaselineStore, clock: Clock): CutoffBaselineManager {
    return CutoffBaselineManager(
        store = store,
        validator = defaultCutoffBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
