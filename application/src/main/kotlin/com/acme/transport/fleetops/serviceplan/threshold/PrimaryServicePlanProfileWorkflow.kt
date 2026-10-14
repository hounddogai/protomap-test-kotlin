package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary service plan profiles through their life cycle and keeps a journal of what it did. */
data class PrimaryServicePlanProfileService(
    var store: PrimaryServicePlanProfileStore,
    var validator: PrimaryServicePlanProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryServicePlanProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary service plan profile $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryServicePlanProfileStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary service plan profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryServicePlanProfileStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary service plan profile $code not found")
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

fun newPrimaryServicePlanProfileService(store: PrimaryServicePlanProfileStore, clock: Clock, flags: Registry): PrimaryServicePlanProfileService {
    return PrimaryServicePlanProfileService(
        store = store,
        validator = defaultPrimaryServicePlanProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryServicePlanProfileEngine(flags),
    )
}
