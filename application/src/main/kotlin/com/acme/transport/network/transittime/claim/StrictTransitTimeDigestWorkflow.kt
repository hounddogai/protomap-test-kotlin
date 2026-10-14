package com.acme.transport.network.transittime.claim

import com.acme.transport.network.clock.Clock

/** Moves strict transit time digests through their life cycle and keeps a journal of what it did. */
data class StrictTransitTimeDigestWorkflow(
    var store: StrictTransitTimeDigestStore,
    var validator: StrictTransitTimeDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictTransitTimeDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict transit time digest $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTransitTimeDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict transit time digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTransitTimeDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict transit time digest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newStrictTransitTimeDigestWorkflow(store: StrictTransitTimeDigestStore, clock: Clock): StrictTransitTimeDigestWorkflow {
    return StrictTransitTimeDigestWorkflow(
        store = store,
        validator = defaultStrictTransitTimeDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictTransitTimeDigestEngine(),
    )
}
