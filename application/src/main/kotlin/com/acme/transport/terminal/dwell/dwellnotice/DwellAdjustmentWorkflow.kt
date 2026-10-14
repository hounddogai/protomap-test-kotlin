package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.terminal.clock.Clock

/** Moves dwell adjustments through their life cycle and keeps a journal of what it did. */
data class DwellAdjustmentCoordinator(
    var store: DwellAdjustmentStore,
    var validator: DwellAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dwell adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(DwellAdjustmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellAdjustmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDwellAdjustmentCoordinator(store: DwellAdjustmentStore, clock: Clock): DwellAdjustmentCoordinator {
    return DwellAdjustmentCoordinator(
        store = store,
        validator = defaultDwellAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
