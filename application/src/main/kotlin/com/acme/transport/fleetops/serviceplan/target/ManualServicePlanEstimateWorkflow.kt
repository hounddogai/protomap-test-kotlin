package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.clock.Clock

/** Moves manual service plan estimates through their life cycle and keeps a journal of what it did. */
data class ManualServicePlanEstimateService(
    var store: ManualServicePlanEstimateStore,
    var validator: ManualServicePlanEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual service plan estimate $code not found")
        validator.requireValid(item)
        item.moveTo(ManualServicePlanEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual service plan estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualServicePlanEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newManualServicePlanEstimateService(store: ManualServicePlanEstimateStore, clock: Clock): ManualServicePlanEstimateService {
    return ManualServicePlanEstimateService(
        store = store,
        validator = defaultManualServicePlanEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
