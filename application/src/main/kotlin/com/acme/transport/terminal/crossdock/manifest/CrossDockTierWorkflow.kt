package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock tiers through their life cycle and keeps a journal of what it did. */
data class CrossDockTierManager(
    var store: CrossDockTierStore,
    var validator: CrossDockTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock tier $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockTierStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockTierStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newCrossDockTierManager(store: CrossDockTierStore, clock: Clock): CrossDockTierManager {
    return CrossDockTierManager(
        store = store,
        validator = defaultCrossDockTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
