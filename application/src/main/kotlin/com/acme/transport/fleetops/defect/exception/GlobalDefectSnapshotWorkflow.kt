package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.clock.Clock

/** Moves global defect snapshots through their life cycle and keeps a journal of what it did. */
data class GlobalDefectSnapshotWorkflow(
    var store: GlobalDefectSnapshotStore,
    var validator: GlobalDefectSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDefectSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global defect snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDefectSnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global defect snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDefectSnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global defect snapshot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newGlobalDefectSnapshotWorkflow(store: GlobalDefectSnapshotStore, clock: Clock): GlobalDefectSnapshotWorkflow {
    return GlobalDefectSnapshotWorkflow(
        store = store,
        validator = defaultGlobalDefectSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDefectSnapshotEngine(),
    )
}
