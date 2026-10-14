package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge plans through their life cycle and keeps a journal of what it did. */
data class EvChargePlanService(
    var store: EvChargePlanStore,
    var validator: EvChargePlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargePlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("ev charge plan $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargePlanStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargePlanStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("ev charge plan $code not found")
        val result = engine.price(item)
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

fun newEvChargePlanService(store: EvChargePlanStore, clock: Clock, flags: Registry): EvChargePlanService {
    return EvChargePlanService(
        store = store,
        validator = defaultEvChargePlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargePlanEngine(flags),
    )
}
