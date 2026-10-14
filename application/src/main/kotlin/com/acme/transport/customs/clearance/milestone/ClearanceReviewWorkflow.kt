package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance reviews through their life cycle and keeps a journal of what it did. */
data class ClearanceReviewDesk(
    var store: ClearanceReviewStore,
    var validator: ClearanceReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("clearance review $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("clearance review $code not found")
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

fun newClearanceReviewDesk(store: ClearanceReviewStore, clock: Clock, flags: Registry): ClearanceReviewDesk {
    return ClearanceReviewDesk(
        store = store,
        validator = defaultClearanceReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceReviewEngine(flags),
    )
}
