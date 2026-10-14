package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves legacy lane plan slots through their life cycle and keeps a journal of what it did. */
data class LegacyLanePlanSlotCoordinator(
    var store: LegacyLanePlanSlotStore,
    var validator: LegacyLanePlanSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyLanePlanSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy lane plan slot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyLanePlanSlotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy lane plan slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyLanePlanSlotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy lane plan slot $code not found")
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

fun newLegacyLanePlanSlotCoordinator(store: LegacyLanePlanSlotStore, clock: Clock, flags: Registry): LegacyLanePlanSlotCoordinator {
    return LegacyLanePlanSlotCoordinator(
        store = store,
        validator = defaultLegacyLanePlanSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyLanePlanSlotEngine(flags),
    )
}
