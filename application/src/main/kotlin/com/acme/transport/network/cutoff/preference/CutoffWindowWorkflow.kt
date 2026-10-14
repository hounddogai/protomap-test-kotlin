package com.acme.transport.network.cutoff.preference

import com.acme.transport.network.clock.Clock

/** Moves cutoff windows through their life cycle and keeps a journal of what it did. */
data class CutoffWindowManager(
    var store: CutoffWindowStore,
    var validator: CutoffWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cutoff window $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCutoffWindowManager(store: CutoffWindowStore, clock: Clock): CutoffWindowManager {
    return CutoffWindowManager(
        store = store,
        validator = defaultCutoffWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
