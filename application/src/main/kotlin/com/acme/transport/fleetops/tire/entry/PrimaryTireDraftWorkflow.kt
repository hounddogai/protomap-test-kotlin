package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.clock.Clock

/** Moves primary tire drafts through their life cycle and keeps a journal of what it did. */
data class PrimaryTireDraftCoordinator(
    var store: PrimaryTireDraftStore,
    var validator: PrimaryTireDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary tire draft $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTireDraftStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary tire draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTireDraftStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPrimaryTireDraftCoordinator(store: PrimaryTireDraftStore, clock: Clock): PrimaryTireDraftCoordinator {
    return PrimaryTireDraftCoordinator(
        store = store,
        validator = defaultPrimaryTireDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
