package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection tiers through their life cycle and keeps a journal of what it did. */
data class InspectionTierManager(
    var store: InspectionTierStore,
    var validator: InspectionTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection tier $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("inspection tier $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newInspectionTierManager(store: InspectionTierStore, clock: Clock): InspectionTierManager {
    return InspectionTierManager(
        store = store,
        validator = defaultInspectionTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionTierEngine(),
    )
}
