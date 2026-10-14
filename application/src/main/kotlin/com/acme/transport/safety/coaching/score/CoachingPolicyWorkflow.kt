package com.acme.transport.safety.coaching.score

import com.acme.transport.safety.clock.Clock

/** Moves coaching policies through their life cycle and keeps a journal of what it did. */
data class CoachingPolicyWorkflow(
    var store: CoachingPolicyStore,
    var validator: CoachingPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("coaching policy $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("coaching policy $code not found")
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

fun newCoachingPolicyWorkflow(store: CoachingPolicyStore, clock: Clock): CoachingPolicyWorkflow {
    return CoachingPolicyWorkflow(
        store = store,
        validator = defaultCoachingPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingPolicyEngine(),
    )
}
