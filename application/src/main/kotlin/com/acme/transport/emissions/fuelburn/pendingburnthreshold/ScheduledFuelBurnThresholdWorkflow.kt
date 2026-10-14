package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.emissions.clock.Clock

/**
 * Moves scheduled fuel burn thresholds through their life cycle and keeps a journal of what it did.
 */
data class ScheduledFuelBurnThresholdWorkflow(
    var store: ScheduledFuelBurnThresholdStore,
    var validator: ScheduledFuelBurnThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled fuel burn threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledFuelBurnThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled fuel burn threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledFuelBurnThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newScheduledFuelBurnThresholdWorkflow(store: ScheduledFuelBurnThresholdStore, clock: Clock): ScheduledFuelBurnThresholdWorkflow {
    return ScheduledFuelBurnThresholdWorkflow(
        store = store,
        validator = defaultScheduledFuelBurnThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
