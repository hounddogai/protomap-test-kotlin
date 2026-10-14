package com.acme.transport.freightpay.audit.rule

import com.acme.transport.freightpay.clock.Clock

/** Moves fallback audit reviews through their life cycle and keeps a journal of what it did. */
data class FallbackAuditReviewCoordinator(
    var store: FallbackAuditReviewStore,
    var validator: FallbackAuditReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackAuditReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback audit review $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackAuditReviewStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback audit review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackAuditReviewStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback audit review $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newFallbackAuditReviewCoordinator(store: FallbackAuditReviewStore, clock: Clock): FallbackAuditReviewCoordinator {
    return FallbackAuditReviewCoordinator(
        store = store,
        validator = defaultFallbackAuditReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackAuditReviewEngine(),
    )
}
