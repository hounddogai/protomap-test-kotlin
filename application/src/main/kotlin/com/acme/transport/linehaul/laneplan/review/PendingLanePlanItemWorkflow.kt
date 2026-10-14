package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.linehaul.clock.Clock

/** Moves pending lane plan items through their life cycle and keeps a journal of what it did. */
data class PendingLanePlanItemCoordinator(
    var store: PendingLanePlanItemStore,
    var validator: PendingLanePlanItemValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending lane plan item $code not found")
        validator.requireValid(item)
        item.moveTo(PendingLanePlanItemStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending lane plan item $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingLanePlanItemStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newPendingLanePlanItemCoordinator(store: PendingLanePlanItemStore, clock: Clock): PendingLanePlanItemCoordinator {
    return PendingLanePlanItemCoordinator(
        store = store,
        validator = defaultPendingLanePlanItemValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
