package com.acme.transport.fleetops.tire.draft

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire adjustments through their life cycle and keeps a journal of what it did. */
data class TireAdjustmentCoordinator(
    var store: TireAdjustmentStore,
    var validator: TireAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tire adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(TireAdjustmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tire adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireAdjustmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tire adjustment $code not found")
        val result = engine.run(item)
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

fun newTireAdjustmentCoordinator(store: TireAdjustmentStore, clock: Clock, flags: Registry): TireAdjustmentCoordinator {
    return TireAdjustmentCoordinator(
        store = store,
        validator = defaultTireAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireAdjustmentEngine(flags),
    )
}
