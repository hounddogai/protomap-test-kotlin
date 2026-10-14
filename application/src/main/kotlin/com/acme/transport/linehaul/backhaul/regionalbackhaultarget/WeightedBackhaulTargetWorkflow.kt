package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves weighted backhaul targets through their life cycle and keeps a journal of what it did. */
data class WeightedBackhaulTargetManager(
    var store: WeightedBackhaulTargetStore,
    var validator: WeightedBackhaulTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedBackhaulTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted backhaul target $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedBackhaulTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted backhaul target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedBackhaulTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted backhaul target $code not found")
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

fun newWeightedBackhaulTargetManager(store: WeightedBackhaulTargetStore, clock: Clock, flags: Registry): WeightedBackhaulTargetManager {
    return WeightedBackhaulTargetManager(
        store = store,
        validator = defaultWeightedBackhaulTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedBackhaulTargetEngine(flags),
    )
}
