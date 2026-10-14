package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves primary lease return schedules through their life cycle and keeps a journal of what it did.
 */
data class PrimaryLeaseReturnScheduleCoordinator(
    var store: PrimaryLeaseReturnScheduleStore,
    var validator: PrimaryLeaseReturnScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryLeaseReturnScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary lease return schedule $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryLeaseReturnScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary lease return schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryLeaseReturnScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary lease return schedule $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newPrimaryLeaseReturnScheduleCoordinator(store: PrimaryLeaseReturnScheduleStore, clock: Clock, flags: Registry): PrimaryLeaseReturnScheduleCoordinator {
    return PrimaryLeaseReturnScheduleCoordinator(
        store = store,
        validator = defaultPrimaryLeaseReturnScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryLeaseReturnScheduleEngine(flags),
    )
}
