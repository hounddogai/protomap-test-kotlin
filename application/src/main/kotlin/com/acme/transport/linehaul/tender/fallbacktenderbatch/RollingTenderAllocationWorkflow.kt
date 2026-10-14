package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.linehaul.clock.Clock

/** Moves rolling tender allocations through their life cycle and keeps a journal of what it did. */
data class RollingTenderAllocationManager(
    var store: RollingTenderAllocationStore,
    var validator: RollingTenderAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingTenderAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling tender allocation $code not found")
        validator.requireValid(item)
        item.moveTo(RollingTenderAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling tender allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingTenderAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling tender allocation $code not found")
        val result = engine.compute(item)
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

fun newRollingTenderAllocationManager(store: RollingTenderAllocationStore, clock: Clock): RollingTenderAllocationManager {
    return RollingTenderAllocationManager(
        store = store,
        validator = defaultRollingTenderAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingTenderAllocationEngine(),
    )
}
