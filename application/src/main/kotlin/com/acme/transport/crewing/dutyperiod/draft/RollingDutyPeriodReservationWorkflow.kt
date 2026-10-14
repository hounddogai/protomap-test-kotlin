package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves rolling duty period reservations through their life cycle and keeps a journal of what it did.
 */
data class RollingDutyPeriodReservationService(
    var store: RollingDutyPeriodReservationStore,
    var validator: RollingDutyPeriodReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDutyPeriodReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling duty period reservation $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDutyPeriodReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling duty period reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDutyPeriodReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling duty period reservation $code not found")
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

fun newRollingDutyPeriodReservationService(store: RollingDutyPeriodReservationStore, clock: Clock, flags: Registry): RollingDutyPeriodReservationService {
    return RollingDutyPeriodReservationService(
        store = store,
        validator = defaultRollingDutyPeriodReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDutyPeriodReservationEngine(flags),
    )
}
