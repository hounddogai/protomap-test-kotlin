package com.acme.transport.crewing.availability.agreement

import com.acme.transport.crewing.clock.Clock

/** Moves availability digests through their life cycle and keeps a journal of what it did. */
data class AvailabilityDigestService(
    var store: AvailabilityDigestStore,
    var validator: AvailabilityDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("availability digest $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityDigestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("availability digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityDigestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newAvailabilityDigestService(store: AvailabilityDigestStore, clock: Clock): AvailabilityDigestService {
    return AvailabilityDigestService(
        store = store,
        validator = defaultAvailabilityDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
