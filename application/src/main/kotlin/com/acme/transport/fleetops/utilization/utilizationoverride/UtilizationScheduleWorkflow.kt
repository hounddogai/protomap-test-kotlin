package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization schedules through their life cycle and keeps a journal of what it did. */
data class UtilizationScheduleCoordinator(
    var store: UtilizationScheduleStore,
    var validator: UtilizationScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("utilization schedule $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("utilization schedule $code not found")
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

fun newUtilizationScheduleCoordinator(store: UtilizationScheduleStore, clock: Clock): UtilizationScheduleCoordinator {
    return UtilizationScheduleCoordinator(
        store = store,
        validator = defaultUtilizationScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationScheduleEngine(),
    )
}
