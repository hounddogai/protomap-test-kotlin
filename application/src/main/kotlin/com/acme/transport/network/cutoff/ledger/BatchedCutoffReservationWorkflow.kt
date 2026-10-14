package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves batched cutoff reservations through their life cycle and keeps a journal of what it did. */
data class BatchedCutoffReservationManager(
    var store: BatchedCutoffReservationStore,
    var validator: BatchedCutoffReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedCutoffReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched cutoff reservation $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedCutoffReservationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched cutoff reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedCutoffReservationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched cutoff reservation $code not found")
        val result = engine.price(item)
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

fun newBatchedCutoffReservationManager(store: BatchedCutoffReservationStore, clock: Clock, flags: Registry): BatchedCutoffReservationManager {
    return BatchedCutoffReservationManager(
        store = store,
        validator = defaultBatchedCutoffReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedCutoffReservationEngine(flags),
    )
}
