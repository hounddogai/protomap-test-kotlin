package com.acme.transport.linehaul.tender.review

import com.acme.transport.linehaul.clock.Clock

/** Moves global tender reviews through their life cycle and keeps a journal of what it did. */
data class GlobalTenderReviewDesk(
    var store: GlobalTenderReviewStore,
    var validator: GlobalTenderReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalTenderReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global tender review $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTenderReviewStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global tender review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTenderReviewStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global tender review $code not found")
        val result = engine.price(item)
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

fun newGlobalTenderReviewDesk(store: GlobalTenderReviewStore, clock: Clock): GlobalTenderReviewDesk {
    return GlobalTenderReviewDesk(
        store = store,
        validator = defaultGlobalTenderReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalTenderReviewEngine(),
    )
}
