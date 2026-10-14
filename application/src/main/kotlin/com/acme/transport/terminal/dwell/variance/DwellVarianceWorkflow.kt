package com.acme.transport.terminal.dwell.variance

import com.acme.transport.terminal.clock.Clock

/** Moves dwell variances through their life cycle and keeps a journal of what it did. */
data class DwellVarianceCoordinator(
    var store: DwellVarianceStore,
    var validator: DwellVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dwell variance $code not found")
        validator.requireValid(item)
        item.moveTo(DwellVarianceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellVarianceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newDwellVarianceCoordinator(store: DwellVarianceStore, clock: Clock): DwellVarianceCoordinator {
    return DwellVarianceCoordinator(
        store = store,
        validator = defaultDwellVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
