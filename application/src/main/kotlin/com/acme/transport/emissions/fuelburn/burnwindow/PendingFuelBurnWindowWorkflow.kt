package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.clock.Clock

/** Moves pending fuel burn windows through their life cycle and keeps a journal of what it did. */
data class PendingFuelBurnWindowManager(
    var store: PendingFuelBurnWindowStore,
    var validator: PendingFuelBurnWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending fuel burn window $code not found")
        validator.requireValid(item)
        item.moveTo(PendingFuelBurnWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending fuel burn window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingFuelBurnWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingFuelBurnWindowManager(store: PendingFuelBurnWindowStore, clock: Clock): PendingFuelBurnWindowManager {
    return PendingFuelBurnWindowManager(
        store = store,
        validator = defaultPendingFuelBurnWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
