package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.terminal.clock.Clock

/** Moves tiered cross dock adjustments through their life cycle and keeps a journal of what it did. */
data class TieredCrossDockAdjustmentCoordinator(
    var store: TieredCrossDockAdjustmentStore,
    var validator: TieredCrossDockAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered cross dock adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCrossDockAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cross dock adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCrossDockAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newTieredCrossDockAdjustmentCoordinator(store: TieredCrossDockAdjustmentStore, clock: Clock): TieredCrossDockAdjustmentCoordinator {
    return TieredCrossDockAdjustmentCoordinator(
        store = store,
        validator = defaultTieredCrossDockAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
