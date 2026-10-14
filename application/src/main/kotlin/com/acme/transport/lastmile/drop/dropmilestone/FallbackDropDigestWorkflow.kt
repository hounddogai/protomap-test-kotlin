package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.lastmile.clock.Clock

/** Moves fallback drop digests through their life cycle and keeps a journal of what it did. */
data class FallbackDropDigestManager(
    var store: FallbackDropDigestStore,
    var validator: FallbackDropDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDropDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback drop digest $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDropDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback drop digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDropDigestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback drop digest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newFallbackDropDigestManager(store: FallbackDropDigestStore, clock: Clock): FallbackDropDigestManager {
    return FallbackDropDigestManager(
        store = store,
        validator = defaultFallbackDropDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDropDigestEngine(),
    )
}
