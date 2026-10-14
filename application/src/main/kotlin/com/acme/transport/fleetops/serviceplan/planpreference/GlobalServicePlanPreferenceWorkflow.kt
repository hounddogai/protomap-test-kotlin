package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves global service plan preferences through their life cycle and keeps a journal of what it did.
 */
data class GlobalServicePlanPreferenceDesk(
    var store: GlobalServicePlanPreferenceStore,
    var validator: GlobalServicePlanPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalServicePlanPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global service plan preference $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalServicePlanPreferenceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global service plan preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalServicePlanPreferenceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global service plan preference $code not found")
        val result = engine.compute(item)
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

fun newGlobalServicePlanPreferenceDesk(store: GlobalServicePlanPreferenceStore, clock: Clock): GlobalServicePlanPreferenceDesk {
    return GlobalServicePlanPreferenceDesk(
        store = store,
        validator = defaultGlobalServicePlanPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalServicePlanPreferenceEngine(),
    )
}
