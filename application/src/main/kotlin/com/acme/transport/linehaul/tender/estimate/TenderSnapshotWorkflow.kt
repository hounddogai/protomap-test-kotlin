package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves tender snapshots through their life cycle and keeps a journal of what it did. */
data class TenderSnapshotManager(
    var store: TenderSnapshotStore,
    var validator: TenderSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tender snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(TenderSnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tender snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderSnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tender snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newTenderSnapshotManager(store: TenderSnapshotStore, clock: Clock, flags: Registry): TenderSnapshotManager {
    return TenderSnapshotManager(
        store = store,
        validator = defaultTenderSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderSnapshotEngine(flags),
    )
}
