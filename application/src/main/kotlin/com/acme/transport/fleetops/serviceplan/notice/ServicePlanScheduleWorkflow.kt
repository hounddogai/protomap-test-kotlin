package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan schedules through their life cycle and keeps a journal of what it did. */
data class ServicePlanScheduleManager(
    var store: ServicePlanScheduleStore,
    var validator: ServicePlanScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service plan schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newServicePlanScheduleManager(store: ServicePlanScheduleStore, clock: Clock): ServicePlanScheduleManager {
    return ServicePlanScheduleManager(
        store = store,
        validator = defaultServicePlanScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
