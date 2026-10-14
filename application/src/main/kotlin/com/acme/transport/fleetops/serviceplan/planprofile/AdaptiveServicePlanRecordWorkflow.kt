package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive service plan records through their life cycle and keeps a journal of what it did. */
data class AdaptiveServicePlanRecordManager(
    var store: AdaptiveServicePlanRecordStore,
    var validator: AdaptiveServicePlanRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveServicePlanRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive service plan record $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveServicePlanRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive service plan record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveServicePlanRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive service plan record $code not found")
        val result = engine.run(item)
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

fun newAdaptiveServicePlanRecordManager(store: AdaptiveServicePlanRecordStore, clock: Clock): AdaptiveServicePlanRecordManager {
    return AdaptiveServicePlanRecordManager(
        store = store,
        validator = defaultAdaptiveServicePlanRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveServicePlanRecordEngine(),
    )
}
