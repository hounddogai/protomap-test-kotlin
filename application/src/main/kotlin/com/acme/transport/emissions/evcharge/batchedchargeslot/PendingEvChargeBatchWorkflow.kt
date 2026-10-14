package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.clock.Clock

/** Moves pending ev charge batches through their life cycle and keeps a journal of what it did. */
data class PendingEvChargeBatchCoordinator(
    var store: PendingEvChargeBatchStore,
    var validator: PendingEvChargeBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending ev charge batch $code not found")
        validator.requireValid(item)
        item.moveTo(PendingEvChargeBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending ev charge batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingEvChargeBatchStatus.EXPIRED)
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

fun newPendingEvChargeBatchCoordinator(store: PendingEvChargeBatchStore, clock: Clock): PendingEvChargeBatchCoordinator {
    return PendingEvChargeBatchCoordinator(
        store = store,
        validator = defaultPendingEvChargeBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
