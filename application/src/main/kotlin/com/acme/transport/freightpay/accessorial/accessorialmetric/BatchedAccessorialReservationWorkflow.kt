package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves batched accessorial reservations through their life cycle and keeps a journal of what it did.
 */
data class BatchedAccessorialReservationDesk(
    var store: BatchedAccessorialReservationStore,
    var validator: BatchedAccessorialReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAccessorialReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched accessorial reservation $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAccessorialReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched accessorial reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAccessorialReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched accessorial reservation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newBatchedAccessorialReservationDesk(store: BatchedAccessorialReservationStore, clock: Clock, flags: Registry): BatchedAccessorialReservationDesk {
    return BatchedAccessorialReservationDesk(
        store = store,
        validator = defaultBatchedAccessorialReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAccessorialReservationEngine(flags),
    )
}
