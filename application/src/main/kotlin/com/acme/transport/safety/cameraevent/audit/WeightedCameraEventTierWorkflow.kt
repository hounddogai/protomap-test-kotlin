package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.safety.clock.Clock

/** Moves weighted camera event tiers through their life cycle and keeps a journal of what it did. */
data class WeightedCameraEventTierCoordinator(
    var store: WeightedCameraEventTierStore,
    var validator: WeightedCameraEventTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted camera event tier $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedCameraEventTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted camera event tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedCameraEventTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newWeightedCameraEventTierCoordinator(store: WeightedCameraEventTierStore, clock: Clock): WeightedCameraEventTierCoordinator {
    return WeightedCameraEventTierCoordinator(
        store = store,
        validator = defaultWeightedCameraEventTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
