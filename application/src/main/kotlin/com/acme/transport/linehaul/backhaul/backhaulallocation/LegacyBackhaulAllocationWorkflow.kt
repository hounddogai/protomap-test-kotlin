package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.linehaul.clock.Clock

/** Moves legacy backhaul allocations through their life cycle and keeps a journal of what it did. */
data class LegacyBackhaulAllocationManager(
    var store: LegacyBackhaulAllocationStore,
    var validator: LegacyBackhaulAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyBackhaulAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy backhaul allocation $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyBackhaulAllocationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy backhaul allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyBackhaulAllocationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy backhaul allocation $code not found")
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

fun newLegacyBackhaulAllocationManager(store: LegacyBackhaulAllocationStore, clock: Clock): LegacyBackhaulAllocationManager {
    return LegacyBackhaulAllocationManager(
        store = store,
        validator = defaultLegacyBackhaulAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyBackhaulAllocationEngine(),
    )
}
