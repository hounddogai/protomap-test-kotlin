package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy tire drafts through their life cycle and keeps a journal of what it did. */
data class LegacyTireDraftManager(
    var store: LegacyTireDraftStore,
    var validator: LegacyTireDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy tire draft $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTireDraftStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy tire draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTireDraftStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLegacyTireDraftManager(store: LegacyTireDraftStore, clock: Clock): LegacyTireDraftManager {
    return LegacyTireDraftManager(
        store = store,
        validator = defaultLegacyTireDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
