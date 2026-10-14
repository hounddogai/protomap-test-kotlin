package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves pending dock door drafts through their life cycle and keeps a journal of what it did. */
data class PendingDockDoorDraftWorkflow(
    var store: PendingDockDoorDraftStore,
    var validator: PendingDockDoorDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingDockDoorDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending dock door draft $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDockDoorDraftStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending dock door draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDockDoorDraftStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending dock door draft $code not found")
        val result = engine.run(item)
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

fun newPendingDockDoorDraftWorkflow(store: PendingDockDoorDraftStore, clock: Clock, flags: Registry): PendingDockDoorDraftWorkflow {
    return PendingDockDoorDraftWorkflow(
        store = store,
        validator = defaultPendingDockDoorDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingDockDoorDraftEngine(flags),
    )
}
