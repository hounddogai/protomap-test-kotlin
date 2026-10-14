package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn records through their life cycle and keeps a journal of what it did. */
data class FuelBurnRecordWorkflow(
    var store: FuelBurnRecordStore,
    var validator: FuelBurnRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel burn record $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnRecordStatus.RETIRED)
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

fun newFuelBurnRecordWorkflow(store: FuelBurnRecordStore, clock: Clock): FuelBurnRecordWorkflow {
    return FuelBurnRecordWorkflow(
        store = store,
        validator = defaultFuelBurnRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
