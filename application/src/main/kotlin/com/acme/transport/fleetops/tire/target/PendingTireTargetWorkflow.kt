package com.acme.transport.fleetops.tire.target

import com.acme.transport.fleetops.clock.Clock

/** Moves pending tire targets through their life cycle and keeps a journal of what it did. */
data class PendingTireTargetCoordinator(
    var store: PendingTireTargetStore,
    var validator: PendingTireTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTireTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending tire target $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTireTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending tire target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTireTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending tire target $code not found")
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

fun newPendingTireTargetCoordinator(store: PendingTireTargetStore, clock: Clock): PendingTireTargetCoordinator {
    return PendingTireTargetCoordinator(
        store = store,
        validator = defaultPendingTireTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTireTargetEngine(),
    )
}
