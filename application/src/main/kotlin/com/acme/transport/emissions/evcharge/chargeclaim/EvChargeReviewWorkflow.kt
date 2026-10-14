package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge reviews through their life cycle and keeps a journal of what it did. */
data class EvChargeReviewService(
    var store: EvChargeReviewStore,
    var validator: EvChargeReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("ev charge review $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("ev charge review $code not found")
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

fun newEvChargeReviewService(store: EvChargeReviewStore, clock: Clock, flags: Registry): EvChargeReviewService {
    return EvChargeReviewService(
        store = store,
        validator = defaultEvChargeReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeReviewEngine(flags),
    )
}
