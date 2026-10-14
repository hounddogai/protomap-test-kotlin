package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock windows through their life cycle and keeps a journal of what it did. */
data class CrossDockWindowService(
    var store: CrossDockWindowStore,
    var validator: CrossDockWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock window $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockWindowStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockWindowStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newCrossDockWindowService(store: CrossDockWindowStore, clock: Clock): CrossDockWindowService {
    return CrossDockWindowService(
        store = store,
        validator = defaultCrossDockWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
