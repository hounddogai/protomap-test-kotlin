package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves capped locker segments through their life cycle and keeps a journal of what it did. */
data class CappedLockerSegmentCoordinator(
    var store: CappedLockerSegmentStore,
    var validator: CappedLockerSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedLockerSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped locker segment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLockerSegmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped locker segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLockerSegmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped locker segment $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newCappedLockerSegmentCoordinator(store: CappedLockerSegmentStore, clock: Clock, flags: Registry): CappedLockerSegmentCoordinator {
    return CappedLockerSegmentCoordinator(
        store = store,
        validator = defaultCappedLockerSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedLockerSegmentEngine(flags),
    )
}
