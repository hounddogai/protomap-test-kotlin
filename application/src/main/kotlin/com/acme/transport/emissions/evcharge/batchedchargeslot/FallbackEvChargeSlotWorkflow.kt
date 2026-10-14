package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback ev charge slots through their life cycle and keeps a journal of what it did. */
data class FallbackEvChargeSlotWorkflow(
    var store: FallbackEvChargeSlotStore,
    var validator: FallbackEvChargeSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackEvChargeSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback ev charge slot $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEvChargeSlotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback ev charge slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEvChargeSlotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback ev charge slot $code not found")
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

fun newFallbackEvChargeSlotWorkflow(store: FallbackEvChargeSlotStore, clock: Clock, flags: Registry): FallbackEvChargeSlotWorkflow {
    return FallbackEvChargeSlotWorkflow(
        store = store,
        validator = defaultFallbackEvChargeSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackEvChargeSlotEngine(flags),
    )
}
