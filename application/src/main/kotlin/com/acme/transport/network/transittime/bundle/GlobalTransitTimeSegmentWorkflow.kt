package com.acme.transport.network.transittime.bundle

import com.acme.transport.network.clock.Clock

/** Moves global transit time segments through their life cycle and keeps a journal of what it did. */
data class GlobalTransitTimeSegmentCoordinator(
    var store: GlobalTransitTimeSegmentStore,
    var validator: GlobalTransitTimeSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global transit time segment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTransitTimeSegmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global transit time segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTransitTimeSegmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newGlobalTransitTimeSegmentCoordinator(store: GlobalTransitTimeSegmentStore, clock: Clock): GlobalTransitTimeSegmentCoordinator {
    return GlobalTransitTimeSegmentCoordinator(
        store = store,
        validator = defaultGlobalTransitTimeSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
