package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling service plan tiers through their life cycle and keeps a journal of what it did. */
data class RollingServicePlanTierManager(
    var store: RollingServicePlanTierStore,
    var validator: RollingServicePlanTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingServicePlanTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling service plan tier $code not found")
        validator.requireValid(item)
        item.moveTo(RollingServicePlanTierStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling service plan tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingServicePlanTierStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling service plan tier $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRollingServicePlanTierManager(store: RollingServicePlanTierStore, clock: Clock, flags: Registry): RollingServicePlanTierManager {
    return RollingServicePlanTierManager(
        store = store,
        validator = defaultRollingServicePlanTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingServicePlanTierEngine(flags),
    )
}
