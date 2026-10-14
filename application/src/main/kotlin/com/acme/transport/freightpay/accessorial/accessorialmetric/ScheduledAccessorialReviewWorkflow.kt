package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.clock.Clock

/** Moves scheduled accessorial reviews through their life cycle and keeps a journal of what it did. */
data class ScheduledAccessorialReviewCoordinator(
    var store: ScheduledAccessorialReviewStore,
    var validator: ScheduledAccessorialReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledAccessorialReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled accessorial review $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAccessorialReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled accessorial review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAccessorialReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled accessorial review $code not found")
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

fun newScheduledAccessorialReviewCoordinator(store: ScheduledAccessorialReviewStore, clock: Clock): ScheduledAccessorialReviewCoordinator {
    return ScheduledAccessorialReviewCoordinator(
        store = store,
        validator = defaultScheduledAccessorialReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledAccessorialReviewEngine(),
    )
}
