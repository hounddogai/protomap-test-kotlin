package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan reservations through their life cycle and keeps a journal of what it did. */
data class LanePlanReservationWorkflow(
    var store: LanePlanReservationStore,
    var validator: LanePlanReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lane plan reservation $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lane plan reservation $code not found")
        val result = engine.price(item)
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

fun newLanePlanReservationWorkflow(store: LanePlanReservationStore, clock: Clock, flags: Registry): LanePlanReservationWorkflow {
    return LanePlanReservationWorkflow(
        store = store,
        validator = defaultLanePlanReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanReservationEngine(flags),
    )
}
