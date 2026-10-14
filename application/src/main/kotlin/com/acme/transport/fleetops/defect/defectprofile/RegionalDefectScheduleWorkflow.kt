package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional defect schedules through their life cycle and keeps a journal of what it did. */
data class RegionalDefectScheduleDesk(
    var store: RegionalDefectScheduleStore,
    var validator: RegionalDefectScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDefectScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional defect schedule $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDefectScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional defect schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDefectScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional defect schedule $code not found")
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

fun newRegionalDefectScheduleDesk(store: RegionalDefectScheduleStore, clock: Clock, flags: Registry): RegionalDefectScheduleDesk {
    return RegionalDefectScheduleDesk(
        store = store,
        validator = defaultRegionalDefectScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDefectScheduleEngine(flags),
    )
}
