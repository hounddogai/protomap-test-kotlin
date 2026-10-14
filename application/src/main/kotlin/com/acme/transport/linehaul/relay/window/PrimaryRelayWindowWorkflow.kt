package com.acme.transport.linehaul.relay.window

import com.acme.transport.linehaul.clock.Clock

/** Moves primary relay windows through their life cycle and keeps a journal of what it did. */
data class PrimaryRelayWindowCoordinator(
    var store: PrimaryRelayWindowStore,
    var validator: PrimaryRelayWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary relay window $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryRelayWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary relay window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryRelayWindowStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newPrimaryRelayWindowCoordinator(store: PrimaryRelayWindowStore, clock: Clock): PrimaryRelayWindowCoordinator {
    return PrimaryRelayWindowCoordinator(
        store = store,
        validator = defaultPrimaryRelayWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
