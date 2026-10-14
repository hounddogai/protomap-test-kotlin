package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.crewing.clock.Clock

/** Moves legacy rest break snapshots through their life cycle and keeps a journal of what it did. */
data class LegacyRestBreakSnapshotWorkflow(
    var store: LegacyRestBreakSnapshotStore,
    var validator: LegacyRestBreakSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy rest break snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyRestBreakSnapshotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy rest break snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyRestBreakSnapshotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newLegacyRestBreakSnapshotWorkflow(store: LegacyRestBreakSnapshotStore, clock: Clock): LegacyRestBreakSnapshotWorkflow {
    return LegacyRestBreakSnapshotWorkflow(
        store = store,
        validator = defaultLegacyRestBreakSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
