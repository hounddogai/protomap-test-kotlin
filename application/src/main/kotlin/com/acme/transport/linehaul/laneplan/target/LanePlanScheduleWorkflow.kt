package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan schedules through their life cycle and keeps a journal of what it did. */
data class LanePlanScheduleService(
    var store: LanePlanScheduleStore,
    var validator: LanePlanScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan schedule $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lane plan schedule $code not found")
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

fun newLanePlanScheduleService(store: LanePlanScheduleStore, clock: Clock, flags: Registry): LanePlanScheduleService {
    return LanePlanScheduleService(
        store = store,
        validator = defaultLanePlanScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanScheduleEngine(flags),
    )
}
