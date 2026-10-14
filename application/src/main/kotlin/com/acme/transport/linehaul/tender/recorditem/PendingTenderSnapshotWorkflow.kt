package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves pending tender snapshots through their life cycle and keeps a journal of what it did. */
data class PendingTenderSnapshotCoordinator(
    var store: PendingTenderSnapshotStore,
    var validator: PendingTenderSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTenderSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending tender snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTenderSnapshotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending tender snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTenderSnapshotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("pending tender snapshot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingTenderSnapshotCoordinator(store: PendingTenderSnapshotStore, clock: Clock, flags: Registry): PendingTenderSnapshotCoordinator {
    return PendingTenderSnapshotCoordinator(
        store = store,
        validator = defaultPendingTenderSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTenderSnapshotEngine(flags),
    )
}
