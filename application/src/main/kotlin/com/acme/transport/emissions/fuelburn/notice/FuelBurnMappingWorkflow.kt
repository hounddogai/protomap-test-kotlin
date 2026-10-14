package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn mappings through their life cycle and keeps a journal of what it did. */
data class FuelBurnMappingWorkflow(
    var store: FuelBurnMappingStore,
    var validator: FuelBurnMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel burn mapping $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel burn mapping $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newFuelBurnMappingWorkflow(store: FuelBurnMappingStore, clock: Clock): FuelBurnMappingWorkflow {
    return FuelBurnMappingWorkflow(
        store = store,
        validator = defaultFuelBurnMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnMappingEngine(),
    )
}
