package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves default locker plans through their life cycle and keeps a journal of what it did. */
data class DefaultLockerPlanCoordinator(
    var store: DefaultLockerPlanStore,
    var validator: DefaultLockerPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultLockerPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default locker plan $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultLockerPlanStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default locker plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultLockerPlanStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default locker plan $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newDefaultLockerPlanCoordinator(store: DefaultLockerPlanStore, clock: Clock, flags: Registry): DefaultLockerPlanCoordinator {
    return DefaultLockerPlanCoordinator(
        store = store,
        validator = defaultDefaultLockerPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultLockerPlanEngine(flags),
    )
}
