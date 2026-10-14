package com.acme.transport.linehaul.capacity.request

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity snapshots through their life cycle and keeps a journal of what it did. */
data class CapacitySnapshotDesk(
    var store: CapacitySnapshotStore,
    var validator: CapacitySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacitySnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capacity snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(CapacitySnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacitySnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capacity snapshot $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newCapacitySnapshotDesk(store: CapacitySnapshotStore, clock: Clock): CapacitySnapshotDesk {
    return CapacitySnapshotDesk(
        store = store,
        validator = defaultCapacitySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacitySnapshotEngine(),
    )
}
