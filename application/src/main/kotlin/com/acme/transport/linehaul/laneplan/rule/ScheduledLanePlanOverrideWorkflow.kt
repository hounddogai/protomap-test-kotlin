package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled lane plan overrides through their life cycle and keeps a journal of what it did. */
data class ScheduledLanePlanOverrideService(
    var store: ScheduledLanePlanOverrideStore,
    var validator: ScheduledLanePlanOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledLanePlanOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled lane plan override $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLanePlanOverrideStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lane plan override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLanePlanOverrideStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled lane plan override $code not found")
        val result = engine.price(item)
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

fun newScheduledLanePlanOverrideService(store: ScheduledLanePlanOverrideStore, clock: Clock, flags: Registry): ScheduledLanePlanOverrideService {
    return ScheduledLanePlanOverrideService(
        store = store,
        validator = defaultScheduledLanePlanOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledLanePlanOverrideEngine(flags),
    )
}
