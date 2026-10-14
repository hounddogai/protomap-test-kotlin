package com.acme.transport.crewing.availability.score

import com.acme.transport.crewing.clock.Clock

/** Moves availability scores through their life cycle and keeps a journal of what it did. */
data class AvailabilityScoreService(
    var store: AvailabilityScoreStore,
    var validator: AvailabilityScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("availability score $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("availability score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newAvailabilityScoreService(store: AvailabilityScoreStore, clock: Clock): AvailabilityScoreService {
    return AvailabilityScoreService(
        store = store,
        validator = defaultAvailabilityScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
