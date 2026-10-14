package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves adaptive lease return schedules through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveLeaseReturnScheduleService(
    var store: AdaptiveLeaseReturnScheduleStore,
    var validator: AdaptiveLeaseReturnScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveLeaseReturnScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive lease return schedule $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveLeaseReturnScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive lease return schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveLeaseReturnScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive lease return schedule $code not found")
        val result = engine.run(item)
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

fun newAdaptiveLeaseReturnScheduleService(store: AdaptiveLeaseReturnScheduleStore, clock: Clock): AdaptiveLeaseReturnScheduleService {
    return AdaptiveLeaseReturnScheduleService(
        store = store,
        validator = defaultAdaptiveLeaseReturnScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveLeaseReturnScheduleEngine(),
    )
}
