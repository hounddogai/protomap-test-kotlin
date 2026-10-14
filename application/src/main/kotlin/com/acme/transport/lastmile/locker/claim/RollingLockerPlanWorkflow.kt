package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves rolling locker plans through their life cycle and keeps a journal of what it did. */
data class RollingLockerPlanManager(
    var store: RollingLockerPlanStore,
    var validator: RollingLockerPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingLockerPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling locker plan $code not found")
        validator.requireValid(item)
        item.moveTo(RollingLockerPlanStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling locker plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingLockerPlanStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling locker plan $code not found")
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

fun newRollingLockerPlanManager(store: RollingLockerPlanStore, clock: Clock, flags: Registry): RollingLockerPlanManager {
    return RollingLockerPlanManager(
        store = store,
        validator = defaultRollingLockerPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingLockerPlanEngine(flags),
    )
}
