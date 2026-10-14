package com.acme.transport.safety.incident.claim

import com.acme.transport.safety.clock.Clock

/** Moves regional incident reviews through their life cycle and keeps a journal of what it did. */
data class RegionalIncidentReviewService(
    var store: RegionalIncidentReviewStore,
    var validator: RegionalIncidentReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalIncidentReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional incident review $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalIncidentReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional incident review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalIncidentReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional incident review $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRegionalIncidentReviewService(store: RegionalIncidentReviewStore, clock: Clock): RegionalIncidentReviewService {
    return RegionalIncidentReviewService(
        store = store,
        validator = defaultRegionalIncidentReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalIncidentReviewEngine(),
    )
}
