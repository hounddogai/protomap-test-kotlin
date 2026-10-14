package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.safety.clock.Clock

/** Moves default coaching claims through their life cycle and keeps a journal of what it did. */
data class DefaultCoachingClaimService(
    var store: DefaultCoachingClaimStore,
    var validator: DefaultCoachingClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCoachingClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default coaching claim $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCoachingClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default coaching claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCoachingClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default coaching claim $code not found")
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

fun newDefaultCoachingClaimService(store: DefaultCoachingClaimStore, clock: Clock): DefaultCoachingClaimService {
    return DefaultCoachingClaimService(
        store = store,
        validator = defaultDefaultCoachingClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCoachingClaimEngine(),
    )
}
