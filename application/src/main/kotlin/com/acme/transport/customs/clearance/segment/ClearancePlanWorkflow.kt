package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.clock.Clock

/** Moves clearance plans through their life cycle and keeps a journal of what it did. */
data class ClearancePlanService(
    var store: ClearancePlanStore,
    var validator: ClearancePlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearancePlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("clearance plan $code not found")
        validator.requireValid(item)
        item.moveTo(ClearancePlanStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearancePlanStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("clearance plan $code not found")
        val result = engine.evaluate(item)
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

fun newClearancePlanService(store: ClearancePlanStore, clock: Clock): ClearancePlanService {
    return ClearancePlanService(
        store = store,
        validator = defaultClearancePlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearancePlanEngine(),
    )
}
