package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.customs.clock.Clock

/**
 * Moves weighted inspection allocations through their life cycle and keeps a journal of what it did.
 */
data class WeightedInspectionAllocationService(
    var store: WeightedInspectionAllocationStore,
    var validator: WeightedInspectionAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted inspection allocation $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedInspectionAllocationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted inspection allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedInspectionAllocationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newWeightedInspectionAllocationService(store: WeightedInspectionAllocationStore, clock: Clock): WeightedInspectionAllocationService {
    return WeightedInspectionAllocationService(
        store = store,
        validator = defaultWeightedInspectionAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
