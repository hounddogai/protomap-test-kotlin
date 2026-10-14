package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/**
 * Moves adaptive lane plan allocations through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveLanePlanAllocationManager(
    var store: AdaptiveLanePlanAllocationStore,
    var validator: AdaptiveLanePlanAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveLanePlanAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive lane plan allocation $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveLanePlanAllocationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive lane plan allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveLanePlanAllocationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive lane plan allocation $code not found")
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

fun newAdaptiveLanePlanAllocationManager(store: AdaptiveLanePlanAllocationStore, clock: Clock, flags: Registry): AdaptiveLanePlanAllocationManager {
    return AdaptiveLanePlanAllocationManager(
        store = store,
        validator = defaultAdaptiveLanePlanAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveLanePlanAllocationEngine(flags),
    )
}
