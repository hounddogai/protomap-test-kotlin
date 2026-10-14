package com.acme.transport.linehaul.tender.summary

import com.acme.transport.linehaul.clock.Clock

/** Moves global tender digests through their life cycle and keeps a journal of what it did. */
data class GlobalTenderDigestCoordinator(
    var store: GlobalTenderDigestStore,
    var validator: GlobalTenderDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global tender digest $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTenderDigestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global tender digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTenderDigestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newGlobalTenderDigestCoordinator(store: GlobalTenderDigestStore, clock: Clock): GlobalTenderDigestCoordinator {
    return GlobalTenderDigestCoordinator(
        store = store,
        validator = defaultGlobalTenderDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
