package com.acme.transport.safety.scorecard.rule

import com.acme.transport.safety.clock.Clock

/** Moves primary scorecard windows through their life cycle and keeps a journal of what it did. */
data class PrimaryScorecardWindowDesk(
    var store: PrimaryScorecardWindowStore,
    var validator: PrimaryScorecardWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryScorecardWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary scorecard window $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryScorecardWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary scorecard window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryScorecardWindowStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary scorecard window $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newPrimaryScorecardWindowDesk(store: PrimaryScorecardWindowStore, clock: Clock): PrimaryScorecardWindowDesk {
    return PrimaryScorecardWindowDesk(
        store = store,
        validator = defaultPrimaryScorecardWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryScorecardWindowEngine(),
    )
}
