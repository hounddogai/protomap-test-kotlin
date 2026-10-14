package com.acme.transport.fleetops.tire.plan

import com.acme.transport.fleetops.clock.Clock

/** Moves pending tire schedules through their life cycle and keeps a journal of what it did. */
data class PendingTireScheduleCoordinator(
    var store: PendingTireScheduleStore,
    var validator: PendingTireScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTireScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending tire schedule $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTireScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending tire schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTireScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending tire schedule $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newPendingTireScheduleCoordinator(store: PendingTireScheduleStore, clock: Clock): PendingTireScheduleCoordinator {
    return PendingTireScheduleCoordinator(
        store = store,
        validator = defaultPendingTireScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTireScheduleEngine(),
    )
}
