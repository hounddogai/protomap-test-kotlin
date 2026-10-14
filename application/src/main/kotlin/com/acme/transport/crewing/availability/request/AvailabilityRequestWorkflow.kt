package com.acme.transport.crewing.availability.request

import com.acme.transport.crewing.clock.Clock

/** Moves availability requests through their life cycle and keeps a journal of what it did. */
data class AvailabilityRequestService(
    var store: AvailabilityRequestStore,
    var validator: AvailabilityRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AvailabilityRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("availability request $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityRequestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("availability request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityRequestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("availability request $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newAvailabilityRequestService(store: AvailabilityRequestStore, clock: Clock): AvailabilityRequestService {
    return AvailabilityRequestService(
        store = store,
        validator = defaultAvailabilityRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAvailabilityRequestEngine(),
    )
}
