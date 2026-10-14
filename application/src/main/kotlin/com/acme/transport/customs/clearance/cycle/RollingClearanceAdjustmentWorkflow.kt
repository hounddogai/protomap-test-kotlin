package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.clock.Clock

/** Moves rolling clearance adjustments through their life cycle and keeps a journal of what it did. */
data class RollingClearanceAdjustmentCoordinator(
    var store: RollingClearanceAdjustmentStore,
    var validator: RollingClearanceAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling clearance adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(RollingClearanceAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling clearance adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingClearanceAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRollingClearanceAdjustmentCoordinator(store: RollingClearanceAdjustmentStore, clock: Clock): RollingClearanceAdjustmentCoordinator {
    return RollingClearanceAdjustmentCoordinator(
        store = store,
        validator = defaultRollingClearanceAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
