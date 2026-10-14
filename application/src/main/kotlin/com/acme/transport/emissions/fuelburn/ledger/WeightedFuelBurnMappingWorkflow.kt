package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.emissions.clock.Clock

/** Moves weighted fuel burn mappings through their life cycle and keeps a journal of what it did. */
data class WeightedFuelBurnMappingService(
    var store: WeightedFuelBurnMappingStore,
    var validator: WeightedFuelBurnMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedFuelBurnMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted fuel burn mapping $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedFuelBurnMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted fuel burn mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedFuelBurnMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted fuel burn mapping $code not found")
        val result = engine.compute(item)
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

fun newWeightedFuelBurnMappingService(store: WeightedFuelBurnMappingStore, clock: Clock): WeightedFuelBurnMappingService {
    return WeightedFuelBurnMappingService(
        store = store,
        validator = defaultWeightedFuelBurnMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedFuelBurnMappingEngine(),
    )
}
