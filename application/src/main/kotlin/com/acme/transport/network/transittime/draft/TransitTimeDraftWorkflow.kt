package com.acme.transport.network.transittime.draft

import com.acme.transport.network.clock.Clock

/** Moves transit time drafts through their life cycle and keeps a journal of what it did. */
data class TransitTimeDraftService(
    var store: TransitTimeDraftStore,
    var validator: TransitTimeDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("transit time draft $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeDraftStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeDraftStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newTransitTimeDraftService(store: TransitTimeDraftStore, clock: Clock): TransitTimeDraftService {
    return TransitTimeDraftService(
        store = store,
        validator = defaultTransitTimeDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
