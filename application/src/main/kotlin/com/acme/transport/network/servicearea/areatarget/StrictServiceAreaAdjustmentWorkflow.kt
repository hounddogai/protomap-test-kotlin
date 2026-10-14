package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.network.clock.Clock

/**
 * Moves strict service area adjustments through their life cycle and keeps a journal of what it did.
 */
data class StrictServiceAreaAdjustmentCoordinator(
    var store: StrictServiceAreaAdjustmentStore,
    var validator: StrictServiceAreaAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict service area adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictServiceAreaAdjustmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict service area adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictServiceAreaAdjustmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newStrictServiceAreaAdjustmentCoordinator(store: StrictServiceAreaAdjustmentStore, clock: Clock): StrictServiceAreaAdjustmentCoordinator {
    return StrictServiceAreaAdjustmentCoordinator(
        store = store,
        validator = defaultStrictServiceAreaAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
