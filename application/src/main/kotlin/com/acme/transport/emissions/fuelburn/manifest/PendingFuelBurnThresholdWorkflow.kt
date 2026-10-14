package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending fuel burn thresholds through their life cycle and keeps a journal of what it did. */
data class PendingFuelBurnThresholdService(
    var store: PendingFuelBurnThresholdStore,
    var validator: PendingFuelBurnThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingFuelBurnThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending fuel burn threshold $code not found")
        validator.requireValid(item)
        item.moveTo(PendingFuelBurnThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending fuel burn threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingFuelBurnThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("pending fuel burn threshold $code not found")
        val result = engine.price(item)
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

fun newPendingFuelBurnThresholdService(store: PendingFuelBurnThresholdStore, clock: Clock, flags: Registry): PendingFuelBurnThresholdService {
    return PendingFuelBurnThresholdService(
        store = store,
        validator = defaultPendingFuelBurnThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingFuelBurnThresholdEngine(flags),
    )
}
