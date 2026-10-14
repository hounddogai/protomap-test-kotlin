package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.linehaul.clock.Clock

/** Moves primary capacity thresholds through their life cycle and keeps a journal of what it did. */
data class PrimaryCapacityThresholdManager(
    var store: PrimaryCapacityThresholdStore,
    var validator: PrimaryCapacityThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary capacity threshold $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCapacityThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary capacity threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCapacityThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPrimaryCapacityThresholdManager(store: PrimaryCapacityThresholdStore, clock: Clock): PrimaryCapacityThresholdManager {
    return PrimaryCapacityThresholdManager(
        store = store,
        validator = defaultPrimaryCapacityThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
