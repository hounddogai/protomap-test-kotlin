package com.acme.transport.network.hub.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub reviews through their life cycle and keeps a journal of what it did. */
data class HubReviewDesk(
    var store: HubReviewStore,
    var validator: HubReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("hub review $code not found")
        validator.requireValid(item)
        item.moveTo(HubReviewStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("hub review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubReviewStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("hub review $code not found")
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

fun newHubReviewDesk(store: HubReviewStore, clock: Clock, flags: Registry): HubReviewDesk {
    return HubReviewDesk(
        store = store,
        validator = defaultHubReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubReviewEngine(flags),
    )
}
