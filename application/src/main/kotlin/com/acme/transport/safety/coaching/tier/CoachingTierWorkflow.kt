package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves coaching tiers through their life cycle and keeps a journal of what it did. */
data class CoachingTierDesk(
    var store: CoachingTierStore,
    var validator: CoachingTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("coaching tier $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingTierStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingTierStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("coaching tier $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCoachingTierDesk(store: CoachingTierStore, clock: Clock, flags: Registry): CoachingTierDesk {
    return CoachingTierDesk(
        store = store,
        validator = defaultCoachingTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingTierEngine(flags),
    )
}
