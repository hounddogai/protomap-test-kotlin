package com.acme.transport.lastmile.drop.target

import com.acme.transport.lastmile.clock.Clock

/** Moves global drop snapshots through their life cycle and keeps a journal of what it did. */
data class GlobalDropSnapshotWorkflow(
    var store: GlobalDropSnapshotStore,
    var validator: GlobalDropSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDropSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global drop snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDropSnapshotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global drop snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDropSnapshotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global drop snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newGlobalDropSnapshotWorkflow(store: GlobalDropSnapshotStore, clock: Clock): GlobalDropSnapshotWorkflow {
    return GlobalDropSnapshotWorkflow(
        store = store,
        validator = defaultGlobalDropSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDropSnapshotEngine(),
    )
}
