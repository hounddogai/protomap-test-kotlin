package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul baselines through their life cycle and keeps a journal of what it did. */
data class BackhaulBaselineManager(
    var store: BackhaulBaselineStore,
    var validator: BackhaulBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("backhaul baseline $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulBaselineStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulBaselineStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newBackhaulBaselineManager(store: BackhaulBaselineStore, clock: Clock): BackhaulBaselineManager {
    return BackhaulBaselineManager(
        store = store,
        validator = defaultBackhaulBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
