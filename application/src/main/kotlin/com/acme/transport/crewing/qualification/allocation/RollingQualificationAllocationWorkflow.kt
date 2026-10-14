package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves rolling qualification allocations through their life cycle and keeps a journal of what it did.
 */
data class RollingQualificationAllocationManager(
    var store: RollingQualificationAllocationStore,
    var validator: RollingQualificationAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingQualificationAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling qualification allocation $code not found")
        validator.requireValid(item)
        item.moveTo(RollingQualificationAllocationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling qualification allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingQualificationAllocationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling qualification allocation $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newRollingQualificationAllocationManager(store: RollingQualificationAllocationStore, clock: Clock, flags: Registry): RollingQualificationAllocationManager {
    return RollingQualificationAllocationManager(
        store = store,
        validator = defaultRollingQualificationAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingQualificationAllocationEngine(flags),
    )
}
