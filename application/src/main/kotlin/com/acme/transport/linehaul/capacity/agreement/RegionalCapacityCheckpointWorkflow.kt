package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves regional capacity checkpoints through their life cycle and keeps a journal of what it did. */
data class RegionalCapacityCheckpointCoordinator(
    var store: RegionalCapacityCheckpointStore,
    var validator: RegionalCapacityCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCapacityCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional capacity checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCapacityCheckpointStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional capacity checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCapacityCheckpointStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional capacity checkpoint $code not found")
        val result = engine.compute(item)
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

fun newRegionalCapacityCheckpointCoordinator(store: RegionalCapacityCheckpointStore, clock: Clock, flags: Registry): RegionalCapacityCheckpointCoordinator {
    return RegionalCapacityCheckpointCoordinator(
        store = store,
        validator = defaultRegionalCapacityCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCapacityCheckpointEngine(flags),
    )
}
