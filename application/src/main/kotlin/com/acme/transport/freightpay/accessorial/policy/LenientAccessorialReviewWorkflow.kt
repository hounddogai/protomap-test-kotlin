package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient accessorial reviews through their life cycle and keeps a journal of what it did. */
data class LenientAccessorialReviewDesk(
    var store: LenientAccessorialReviewStore,
    var validator: LenientAccessorialReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientAccessorialReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient accessorial review $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAccessorialReviewStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient accessorial review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAccessorialReviewStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient accessorial review $code not found")
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

fun newLenientAccessorialReviewDesk(store: LenientAccessorialReviewStore, clock: Clock, flags: Registry): LenientAccessorialReviewDesk {
    return LenientAccessorialReviewDesk(
        store = store,
        validator = defaultLenientAccessorialReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientAccessorialReviewEngine(flags),
    )
}
