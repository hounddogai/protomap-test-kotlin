package com.acme.transport.freightpay.audit.review

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves audit reviews through their life cycle and keeps a journal of what it did. */
data class AuditReviewDesk(
    var store: AuditReviewStore,
    var validator: AuditReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("audit review $code not found")
        validator.requireValid(item)
        item.moveTo(AuditReviewStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("audit review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditReviewStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("audit review $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newAuditReviewDesk(store: AuditReviewStore, clock: Clock, flags: Registry): AuditReviewDesk {
    return AuditReviewDesk(
        store = store,
        validator = defaultAuditReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditReviewEngine(flags),
    )
}
