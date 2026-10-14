package com.acme.transport.freightpay.dispute.request

import com.acme.transport.freightpay.clock.Clock

/** Moves batched dispute reservations through their life cycle and keeps a journal of what it did. */
data class BatchedDisputeReservationDesk(
    var store: BatchedDisputeReservationStore,
    var validator: BatchedDisputeReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDisputeReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched dispute reservation $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDisputeReservationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched dispute reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDisputeReservationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched dispute reservation $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newBatchedDisputeReservationDesk(store: BatchedDisputeReservationStore, clock: Clock): BatchedDisputeReservationDesk {
    return BatchedDisputeReservationDesk(
        store = store,
        validator = defaultBatchedDisputeReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDisputeReservationEngine(),
    )
}
