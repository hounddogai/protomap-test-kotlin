package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay adjustments through their life cycle and keeps a journal of what it did. */
data class RelayAdjustmentCoordinator(
    var store: RelayAdjustmentStore,
    var validator: RelayAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("relay adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(RelayAdjustmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("relay adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayAdjustmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("relay adjustment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRelayAdjustmentCoordinator(store: RelayAdjustmentStore, clock: Clock, flags: Registry): RelayAdjustmentCoordinator {
    return RelayAdjustmentCoordinator(
        store = store,
        validator = defaultRelayAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayAdjustmentEngine(flags),
    )
}
