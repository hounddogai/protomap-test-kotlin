package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capacity reviews through their life cycle and keeps a journal of what it did. */
data class CapacityReviewService(
    var store: CapacityReviewStore,
    var validator: CapacityReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capacity review $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityReviewStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityReviewStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capacity review $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newCapacityReviewService(store: CapacityReviewStore, clock: Clock, flags: Registry): CapacityReviewService {
    return CapacityReviewService(
        store = store,
        validator = defaultCapacityReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityReviewEngine(flags),
    )
}
