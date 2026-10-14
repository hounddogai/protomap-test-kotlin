package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.clock.Clock

/**
 * Moves primary availability thresholds through their life cycle and keeps a journal of what it did.
 */
data class PrimaryAvailabilityThresholdDesk(
    var store: PrimaryAvailabilityThresholdStore,
    var validator: PrimaryAvailabilityThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryAvailabilityThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary availability threshold $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAvailabilityThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary availability threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAvailabilityThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary availability threshold $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newPrimaryAvailabilityThresholdDesk(store: PrimaryAvailabilityThresholdStore, clock: Clock): PrimaryAvailabilityThresholdDesk {
    return PrimaryAvailabilityThresholdDesk(
        store = store,
        validator = defaultPrimaryAvailabilityThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryAvailabilityThresholdEngine(),
    )
}
