package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan reservations through their life cycle and keeps a journal of what it did. */
data class ServicePlanReservationService(
    var store: ServicePlanReservationStore,
    var validator: ServicePlanReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service plan reservation $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanReservationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanReservationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("service plan reservation $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newServicePlanReservationService(store: ServicePlanReservationStore, clock: Clock): ServicePlanReservationService {
    return ServicePlanReservationService(
        store = store,
        validator = defaultServicePlanReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanReservationEngine(),
    )
}
