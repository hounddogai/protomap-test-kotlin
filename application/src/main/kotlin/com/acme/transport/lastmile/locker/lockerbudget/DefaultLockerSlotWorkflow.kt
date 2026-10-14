package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves default locker slots through their life cycle and keeps a journal of what it did. */
data class DefaultLockerSlotManager(
    var store: DefaultLockerSlotStore,
    var validator: DefaultLockerSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultLockerSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default locker slot $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultLockerSlotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default locker slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultLockerSlotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default locker slot $code not found")
        val result = engine.run(item)
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

fun newDefaultLockerSlotManager(store: DefaultLockerSlotStore, clock: Clock, flags: Registry): DefaultLockerSlotManager {
    return DefaultLockerSlotManager(
        store = store,
        validator = defaultDefaultLockerSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultLockerSlotEngine(flags),
    )
}
