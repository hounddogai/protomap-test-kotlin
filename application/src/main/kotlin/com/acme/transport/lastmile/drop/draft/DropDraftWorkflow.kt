package com.acme.transport.lastmile.drop.draft

import com.acme.transport.lastmile.clock.Clock

/** Moves drop drafts through their life cycle and keeps a journal of what it did. */
data class DropDraftWorkflow(
    var store: DropDraftStore,
    var validator: DropDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("drop draft $code not found")
        validator.requireValid(item)
        item.moveTo(DropDraftStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("drop draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropDraftStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newDropDraftWorkflow(store: DropDraftStore, clock: Clock): DropDraftWorkflow {
    return DropDraftWorkflow(
        store = store,
        validator = defaultDropDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
