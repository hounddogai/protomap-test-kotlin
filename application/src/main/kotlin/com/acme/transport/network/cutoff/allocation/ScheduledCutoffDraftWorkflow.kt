package com.acme.transport.network.cutoff.allocation

import com.acme.transport.network.clock.Clock

/** Moves scheduled cutoff drafts through their life cycle and keeps a journal of what it did. */
data class ScheduledCutoffDraftDesk(
    var store: ScheduledCutoffDraftStore,
    var validator: ScheduledCutoffDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledCutoffDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled cutoff draft $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCutoffDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled cutoff draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCutoffDraftStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled cutoff draft $code not found")
        val result = engine.price(item)
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

fun newScheduledCutoffDraftDesk(store: ScheduledCutoffDraftStore, clock: Clock): ScheduledCutoffDraftDesk {
    return ScheduledCutoffDraftDesk(
        store = store,
        validator = defaultScheduledCutoffDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledCutoffDraftEngine(),
    )
}
