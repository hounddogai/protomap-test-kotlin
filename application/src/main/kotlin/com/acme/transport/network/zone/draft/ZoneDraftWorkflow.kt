package com.acme.transport.network.zone.draft

import com.acme.transport.network.clock.Clock

/** Moves zone drafts through their life cycle and keeps a journal of what it did. */
data class ZoneDraftWorkflow(
    var store: ZoneDraftStore,
    var validator: ZoneDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("zone draft $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneDraftStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("zone draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneDraftStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newZoneDraftWorkflow(store: ZoneDraftStore, clock: Clock): ZoneDraftWorkflow {
    return ZoneDraftWorkflow(
        store = store,
        validator = defaultZoneDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
