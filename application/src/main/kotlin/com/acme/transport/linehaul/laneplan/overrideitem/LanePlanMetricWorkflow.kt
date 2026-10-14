package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan metrics through their life cycle and keeps a journal of what it did. */
data class LanePlanMetricService(
    var store: LanePlanMetricStore,
    var validator: LanePlanMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan metric $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lane plan metric $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newLanePlanMetricService(store: LanePlanMetricStore, clock: Clock): LanePlanMetricService {
    return LanePlanMetricService(
        store = store,
        validator = defaultLanePlanMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanMetricEngine(),
    )
}
