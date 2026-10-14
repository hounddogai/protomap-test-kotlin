package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity plans through their life cycle and keeps a journal of what it did. */
data class CapacityPlanService(
    var store: CapacityPlanStore,
    var validator: CapacityPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capacity plan $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityPlanStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityPlanStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capacity plan $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCapacityPlanService(store: CapacityPlanStore, clock: Clock): CapacityPlanService {
    return CapacityPlanService(
        store = store,
        validator = defaultCapacityPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityPlanEngine(),
    )
}
