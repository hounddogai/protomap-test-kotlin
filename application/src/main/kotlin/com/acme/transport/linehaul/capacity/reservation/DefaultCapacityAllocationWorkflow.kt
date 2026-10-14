package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.linehaul.clock.Clock

/** Moves default capacity allocations through their life cycle and keeps a journal of what it did. */
data class DefaultCapacityAllocationCoordinator(
    var store: DefaultCapacityAllocationStore,
    var validator: DefaultCapacityAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCapacityAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default capacity allocation $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCapacityAllocationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default capacity allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCapacityAllocationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default capacity allocation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultCapacityAllocationCoordinator(store: DefaultCapacityAllocationStore, clock: Clock): DefaultCapacityAllocationCoordinator {
    return DefaultCapacityAllocationCoordinator(
        store = store,
        validator = defaultDefaultCapacityAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCapacityAllocationEngine(),
    )
}
