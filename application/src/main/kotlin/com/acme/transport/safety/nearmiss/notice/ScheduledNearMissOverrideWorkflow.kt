package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.safety.clock.Clock

/** Moves scheduled near miss overrides through their life cycle and keeps a journal of what it did. */
data class ScheduledNearMissOverrideManager(
    var store: ScheduledNearMissOverrideStore,
    var validator: ScheduledNearMissOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled near miss override $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledNearMissOverrideStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled near miss override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledNearMissOverrideStatus.STOPPED)
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

fun newScheduledNearMissOverrideManager(store: ScheduledNearMissOverrideStore, clock: Clock): ScheduledNearMissOverrideManager {
    return ScheduledNearMissOverrideManager(
        store = store,
        validator = defaultScheduledNearMissOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
