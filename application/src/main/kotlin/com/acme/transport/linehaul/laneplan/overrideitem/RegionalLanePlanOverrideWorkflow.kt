package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves regional lane plan overrides through their life cycle and keeps a journal of what it did. */
data class RegionalLanePlanOverrideService(
    var store: RegionalLanePlanOverrideStore,
    var validator: RegionalLanePlanOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalLanePlanOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional lane plan override $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalLanePlanOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional lane plan override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalLanePlanOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional lane plan override $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newRegionalLanePlanOverrideService(store: RegionalLanePlanOverrideStore, clock: Clock, flags: Registry): RegionalLanePlanOverrideService {
    return RegionalLanePlanOverrideService(
        store = store,
        validator = defaultRegionalLanePlanOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalLanePlanOverrideEngine(flags),
    )
}
