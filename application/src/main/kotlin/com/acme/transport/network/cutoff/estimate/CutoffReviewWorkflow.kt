package com.acme.transport.network.cutoff.estimate

import com.acme.transport.network.clock.Clock

/** Moves cutoff reviews through their life cycle and keeps a journal of what it did. */
data class CutoffReviewWorkflow(
    var store: CutoffReviewStore,
    var validator: CutoffReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("cutoff review $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffReviewStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffReviewStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("cutoff review $code not found")
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

fun newCutoffReviewWorkflow(store: CutoffReviewStore, clock: Clock): CutoffReviewWorkflow {
    return CutoffReviewWorkflow(
        store = store,
        validator = defaultCutoffReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffReviewEngine(),
    )
}
