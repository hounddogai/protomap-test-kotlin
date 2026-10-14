package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.clock.Clock

/** Moves pending declaration snapshots through their life cycle and keeps a journal of what it did. */
data class PendingDeclarationSnapshotWorkflow(
    var store: PendingDeclarationSnapshotStore,
    var validator: PendingDeclarationSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending declaration snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDeclarationSnapshotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending declaration snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDeclarationSnapshotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPendingDeclarationSnapshotWorkflow(store: PendingDeclarationSnapshotStore, clock: Clock): PendingDeclarationSnapshotWorkflow {
    return PendingDeclarationSnapshotWorkflow(
        store = store,
        validator = defaultPendingDeclarationSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
