package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves manual service plan preferences through their life cycle and keeps a journal of what it did.
 */
data class ManualServicePlanPreferenceService(
    var store: ManualServicePlanPreferenceStore,
    var validator: ManualServicePlanPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual service plan preference $code not found")
        validator.requireValid(item)
        item.moveTo(ManualServicePlanPreferenceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual service plan preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualServicePlanPreferenceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newManualServicePlanPreferenceService(store: ManualServicePlanPreferenceStore, clock: Clock): ManualServicePlanPreferenceService {
    return ManualServicePlanPreferenceService(
        store = store,
        validator = defaultManualServicePlanPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
