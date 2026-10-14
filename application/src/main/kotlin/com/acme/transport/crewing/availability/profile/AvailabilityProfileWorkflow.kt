package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.clock.Clock

/** Moves availability profiles through their life cycle and keeps a journal of what it did. */
data class AvailabilityProfileWorkflow(
    var store: AvailabilityProfileStore,
    var validator: AvailabilityProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("availability profile $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityProfileStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("availability profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityProfileStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newAvailabilityProfileWorkflow(store: AvailabilityProfileStore, clock: Clock): AvailabilityProfileWorkflow {
    return AvailabilityProfileWorkflow(
        store = store,
        validator = defaultAvailabilityProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
