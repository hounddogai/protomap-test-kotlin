package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.linehaul.clock.Clock

/** Moves weighted trailer mappings through their life cycle and keeps a journal of what it did. */
data class WeightedTrailerMappingCoordinator(
    var store: WeightedTrailerMappingStore,
    var validator: WeightedTrailerMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted trailer mapping $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedTrailerMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted trailer mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedTrailerMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newWeightedTrailerMappingCoordinator(store: WeightedTrailerMappingStore, clock: Clock): WeightedTrailerMappingCoordinator {
    return WeightedTrailerMappingCoordinator(
        store = store,
        validator = defaultWeightedTrailerMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
