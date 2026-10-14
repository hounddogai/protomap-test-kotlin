package com.acme.transport.linehaul.relay.review

import com.acme.transport.linehaul.clock.Clock

/** Moves strict relay windows through their life cycle and keeps a journal of what it did. */
data class StrictRelayWindowCoordinator(
    var store: StrictRelayWindowStore,
    var validator: StrictRelayWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict relay window $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRelayWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict relay window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRelayWindowStatus.ARCHIVED)
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

fun newStrictRelayWindowCoordinator(store: StrictRelayWindowStore, clock: Clock): StrictRelayWindowCoordinator {
    return StrictRelayWindowCoordinator(
        store = store,
        validator = defaultStrictRelayWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
