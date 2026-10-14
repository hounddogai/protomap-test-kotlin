package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.lastmile.clock.Clock

/** Moves drop overrides through their life cycle and keeps a journal of what it did. */
data class DropOverrideDesk(
    var store: DropOverrideStore,
    var validator: DropOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("drop override $code not found")
        validator.requireValid(item)
        item.moveTo(DropOverrideStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("drop override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropOverrideStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newDropOverrideDesk(store: DropOverrideStore, clock: Clock): DropOverrideDesk {
    return DropOverrideDesk(
        store = store,
        validator = defaultDropOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
