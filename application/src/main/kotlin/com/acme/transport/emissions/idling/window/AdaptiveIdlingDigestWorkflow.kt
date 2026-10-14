package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.clock.Clock

/** Moves adaptive idling digests through their life cycle and keeps a journal of what it did. */
data class AdaptiveIdlingDigestCoordinator(
    var store: AdaptiveIdlingDigestStore,
    var validator: AdaptiveIdlingDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive idling digest $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveIdlingDigestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive idling digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveIdlingDigestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newAdaptiveIdlingDigestCoordinator(store: AdaptiveIdlingDigestStore, clock: Clock): AdaptiveIdlingDigestCoordinator {
    return AdaptiveIdlingDigestCoordinator(
        store = store,
        validator = defaultAdaptiveIdlingDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
