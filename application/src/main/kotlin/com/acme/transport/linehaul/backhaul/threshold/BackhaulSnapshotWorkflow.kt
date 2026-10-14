package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul snapshots through their life cycle and keeps a journal of what it did. */
data class BackhaulSnapshotService(
    var store: BackhaulSnapshotStore,
    var validator: BackhaulSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("backhaul snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("backhaul snapshot $code not found")
        val result = engine.evaluate(item)
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

fun newBackhaulSnapshotService(store: BackhaulSnapshotStore, clock: Clock): BackhaulSnapshotService {
    return BackhaulSnapshotService(
        store = store,
        validator = defaultBackhaulSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulSnapshotEngine(),
    )
}
