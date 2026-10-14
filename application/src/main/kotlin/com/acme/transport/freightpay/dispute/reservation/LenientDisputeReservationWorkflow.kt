package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient dispute reservations through their life cycle and keeps a journal of what it did. */
data class LenientDisputeReservationWorkflow(
    var store: LenientDisputeReservationStore,
    var validator: LenientDisputeReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDisputeReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient dispute reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDisputeReservationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dispute reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDisputeReservationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient dispute reservation $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newLenientDisputeReservationWorkflow(store: LenientDisputeReservationStore, clock: Clock, flags: Registry): LenientDisputeReservationWorkflow {
    return LenientDisputeReservationWorkflow(
        store = store,
        validator = defaultLenientDisputeReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDisputeReservationEngine(flags),
    )
}
