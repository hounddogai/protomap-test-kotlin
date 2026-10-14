package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capped relay reviews through their life cycle and keeps a journal of what it did. */
data class CappedRelayReviewService(
    var store: CappedRelayReviewStore,
    var validator: CappedRelayReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedRelayReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped relay review $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRelayReviewStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped relay review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRelayReviewStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped relay review $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newCappedRelayReviewService(store: CappedRelayReviewStore, clock: Clock, flags: Registry): CappedRelayReviewService {
    return CappedRelayReviewService(
        store = store,
        validator = defaultCappedRelayReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedRelayReviewEngine(flags),
    )
}
