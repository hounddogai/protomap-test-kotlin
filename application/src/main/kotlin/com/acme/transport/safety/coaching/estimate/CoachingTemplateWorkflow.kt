package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves coaching templates through their life cycle and keeps a journal of what it did. */
data class CoachingTemplateCoordinator(
    var store: CoachingTemplateStore,
    var validator: CoachingTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("coaching template $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingTemplateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingTemplateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("coaching template $code not found")
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

fun newCoachingTemplateCoordinator(store: CoachingTemplateStore, clock: Clock, flags: Registry): CoachingTemplateCoordinator {
    return CoachingTemplateCoordinator(
        store = store,
        validator = defaultCoachingTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingTemplateEngine(flags),
    )
}
