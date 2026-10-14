package com.acme.transport.emissions.idling.digest

import com.acme.transport.emissions.clock.Clock

/** Moves tiered idling digests through their life cycle and keeps a journal of what it did. */
data class TieredIdlingDigestManager(
    var store: TieredIdlingDigestStore,
    var validator: TieredIdlingDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered idling digest $code not found")
        validator.requireValid(item)
        item.moveTo(TieredIdlingDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered idling digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredIdlingDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredIdlingDigestManager(store: TieredIdlingDigestStore, clock: Clock): TieredIdlingDigestManager {
    return TieredIdlingDigestManager(
        store = store,
        validator = defaultTieredIdlingDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
