package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.crewing.clock.Clock

/** Moves legacy roster schedules through their life cycle and keeps a journal of what it did. */
data class LegacyRosterScheduleCoordinator(
    var store: LegacyRosterScheduleStore,
    var validator: LegacyRosterScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy roster schedule $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyRosterScheduleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy roster schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyRosterScheduleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLegacyRosterScheduleCoordinator(store: LegacyRosterScheduleStore, clock: Clock): LegacyRosterScheduleCoordinator {
    return LegacyRosterScheduleCoordinator(
        store = store,
        validator = defaultLegacyRosterScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
