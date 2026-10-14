package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.clock.Clock

/** Moves availability rollups through their life cycle and keeps a journal of what it did. */
data class AvailabilityRollupService(
    var store: AvailabilityRollupStore,
    var validator: AvailabilityRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("availability rollup $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("availability rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAvailabilityRollupService(store: AvailabilityRollupStore, clock: Clock): AvailabilityRollupService {
    return AvailabilityRollupService(
        store = store,
        validator = defaultAvailabilityRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
