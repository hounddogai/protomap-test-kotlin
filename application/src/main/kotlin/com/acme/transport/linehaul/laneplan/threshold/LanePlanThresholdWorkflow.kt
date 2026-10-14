package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan thresholds through their life cycle and keeps a journal of what it did. */
data class LanePlanThresholdService(
    var store: LanePlanThresholdStore,
    var validator: LanePlanThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lane plan threshold $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLanePlanThresholdService(store: LanePlanThresholdStore, clock: Clock): LanePlanThresholdService {
    return LanePlanThresholdService(
        store = store,
        validator = defaultLanePlanThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
