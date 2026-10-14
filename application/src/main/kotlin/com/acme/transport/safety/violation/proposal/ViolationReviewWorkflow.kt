package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation reviews through their life cycle and keeps a journal of what it did. */
data class ViolationReviewCoordinator(
    var store: ViolationReviewStore,
    var validator: ViolationReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("violation review $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationReviewStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("violation review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationReviewStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("violation review $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newViolationReviewCoordinator(store: ViolationReviewStore, clock: Clock, flags: Registry): ViolationReviewCoordinator {
    return ViolationReviewCoordinator(
        store = store,
        validator = defaultViolationReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationReviewEngine(flags),
    )
}
