package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves locker entries through their life cycle and keeps a journal of what it did. */
data class LockerEntryWorkflow(
    var store: LockerEntryStore,
    var validator: LockerEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("locker entry $code not found")
        validator.requireValid(item)
        item.moveTo(LockerEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("locker entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("locker entry $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLockerEntryWorkflow(store: LockerEntryStore, clock: Clock, flags: Registry): LockerEntryWorkflow {
    return LockerEntryWorkflow(
        store = store,
        validator = defaultLockerEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerEntryEngine(flags),
    )
}
