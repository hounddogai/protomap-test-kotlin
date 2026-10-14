package com.acme.transport.network.hub.milestone

import com.acme.transport.network.clock.Clock

/** Moves capped hub baselines through their life cycle and keeps a journal of what it did. */
data class CappedHubBaselineManager(
    var store: CappedHubBaselineStore,
    var validator: CappedHubBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedHubBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped hub baseline $code not found")
        validator.requireValid(item)
        item.moveTo(CappedHubBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped hub baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedHubBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped hub baseline $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newCappedHubBaselineManager(store: CappedHubBaselineStore, clock: Clock): CappedHubBaselineManager {
    return CappedHubBaselineManager(
        store = store,
        validator = defaultCappedHubBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedHubBaselineEngine(),
    )
}
