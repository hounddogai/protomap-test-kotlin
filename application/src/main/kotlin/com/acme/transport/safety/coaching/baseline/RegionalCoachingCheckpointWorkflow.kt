package com.acme.transport.safety.coaching.baseline

import com.acme.transport.safety.clock.Clock

/** Moves regional coaching checkpoints through their life cycle and keeps a journal of what it did. */
data class RegionalCoachingCheckpointDesk(
    var store: RegionalCoachingCheckpointStore,
    var validator: RegionalCoachingCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional coaching checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCoachingCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional coaching checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCoachingCheckpointStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRegionalCoachingCheckpointDesk(store: RegionalCoachingCheckpointStore, clock: Clock): RegionalCoachingCheckpointDesk {
    return RegionalCoachingCheckpointDesk(
        store = store,
        validator = defaultRegionalCoachingCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
