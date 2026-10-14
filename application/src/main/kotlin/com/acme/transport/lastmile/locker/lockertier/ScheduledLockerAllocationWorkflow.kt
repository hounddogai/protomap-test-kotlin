package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled locker allocations through their life cycle and keeps a journal of what it did. */
data class ScheduledLockerAllocationService(
    var store: ScheduledLockerAllocationStore,
    var validator: ScheduledLockerAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledLockerAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled locker allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLockerAllocationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled locker allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLockerAllocationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled locker allocation $code not found")
        val result = engine.evaluate(item)
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

fun newScheduledLockerAllocationService(store: ScheduledLockerAllocationStore, clock: Clock): ScheduledLockerAllocationService {
    return ScheduledLockerAllocationService(
        store = store,
        validator = defaultScheduledLockerAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledLockerAllocationEngine(),
    )
}
