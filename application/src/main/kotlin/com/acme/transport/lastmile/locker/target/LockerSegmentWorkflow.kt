package com.acme.transport.lastmile.locker.target

import com.acme.transport.lastmile.clock.Clock

/** Moves locker segments through their life cycle and keeps a journal of what it did. */
data class LockerSegmentWorkflow(
    var store: LockerSegmentStore,
    var validator: LockerSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("locker segment $code not found")
        validator.requireValid(item)
        item.moveTo(LockerSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("locker segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("locker segment $code not found")
        val result = engine.price(item)
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

fun newLockerSegmentWorkflow(store: LockerSegmentStore, clock: Clock): LockerSegmentWorkflow {
    return LockerSegmentWorkflow(
        store = store,
        validator = defaultLockerSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerSegmentEngine(),
    )
}
