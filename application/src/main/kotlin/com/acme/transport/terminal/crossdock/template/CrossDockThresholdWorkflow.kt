package com.acme.transport.terminal.crossdock.template

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock thresholds through their life cycle and keeps a journal of what it did. */
data class CrossDockThresholdManager(
    var store: CrossDockThresholdStore,
    var validator: CrossDockThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("cross dock threshold $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newCrossDockThresholdManager(store: CrossDockThresholdStore, clock: Clock): CrossDockThresholdManager {
    return CrossDockThresholdManager(
        store = store,
        validator = defaultCrossDockThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
