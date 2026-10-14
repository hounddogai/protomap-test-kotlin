package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge checkpoints through their life cycle and keeps a journal of what it did. */
data class EvChargeCheckpointCoordinator(
    var store: EvChargeCheckpointStore,
    var validator: EvChargeCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("ev charge checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeCheckpointStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeCheckpointStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("ev charge checkpoint $code not found")
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

fun newEvChargeCheckpointCoordinator(store: EvChargeCheckpointStore, clock: Clock, flags: Registry): EvChargeCheckpointCoordinator {
    return EvChargeCheckpointCoordinator(
        store = store,
        validator = defaultEvChargeCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeCheckpointEngine(flags),
    )
}
