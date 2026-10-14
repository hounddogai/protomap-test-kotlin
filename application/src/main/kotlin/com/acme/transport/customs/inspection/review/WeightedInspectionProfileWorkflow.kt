package com.acme.transport.customs.inspection.review

import com.acme.transport.customs.clock.Clock

/** Moves weighted inspection profiles through their life cycle and keeps a journal of what it did. */
data class WeightedInspectionProfileService(
    var store: WeightedInspectionProfileStore,
    var validator: WeightedInspectionProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedInspectionProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted inspection profile $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedInspectionProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted inspection profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedInspectionProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted inspection profile $code not found")
        val result = engine.run(item)
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

fun newWeightedInspectionProfileService(store: WeightedInspectionProfileStore, clock: Clock): WeightedInspectionProfileService {
    return WeightedInspectionProfileService(
        store = store,
        validator = defaultWeightedInspectionProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedInspectionProfileEngine(),
    )
}
