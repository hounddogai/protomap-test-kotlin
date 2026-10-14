package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled trailer windows through their life cycle and keeps a journal of what it did. */
data class ScheduledTrailerWindowCoordinator(
    var store: ScheduledTrailerWindowStore,
    var validator: ScheduledTrailerWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledTrailerWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled trailer window $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTrailerWindowStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled trailer window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTrailerWindowStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled trailer window $code not found")
        val result = engine.compute(item)
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

fun newScheduledTrailerWindowCoordinator(store: ScheduledTrailerWindowStore, clock: Clock): ScheduledTrailerWindowCoordinator {
    return ScheduledTrailerWindowCoordinator(
        store = store,
        validator = defaultScheduledTrailerWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledTrailerWindowEngine(),
    )
}
