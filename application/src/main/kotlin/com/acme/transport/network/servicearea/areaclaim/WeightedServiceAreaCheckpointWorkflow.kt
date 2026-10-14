package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.network.clock.Clock

/**
 * Moves weighted service area checkpoints through their life cycle and keeps a journal of what it did.
 */
data class WeightedServiceAreaCheckpointCoordinator(
    var store: WeightedServiceAreaCheckpointStore,
    var validator: WeightedServiceAreaCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedServiceAreaCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted service area checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedServiceAreaCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted service area checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedServiceAreaCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted service area checkpoint $code not found")
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

fun newWeightedServiceAreaCheckpointCoordinator(store: WeightedServiceAreaCheckpointStore, clock: Clock): WeightedServiceAreaCheckpointCoordinator {
    return WeightedServiceAreaCheckpointCoordinator(
        store = store,
        validator = defaultWeightedServiceAreaCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedServiceAreaCheckpointEngine(),
    )
}
