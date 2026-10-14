package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.network.clock.Clock

/** Moves weighted service area claims through their life cycle and keeps a journal of what it did. */
data class WeightedServiceAreaClaimManager(
    var store: WeightedServiceAreaClaimStore,
    var validator: WeightedServiceAreaClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted service area claim $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedServiceAreaClaimStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted service area claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedServiceAreaClaimStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newWeightedServiceAreaClaimManager(store: WeightedServiceAreaClaimStore, clock: Clock): WeightedServiceAreaClaimManager {
    return WeightedServiceAreaClaimManager(
        store = store,
        validator = defaultWeightedServiceAreaClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
