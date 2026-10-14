package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves global coaching baselines through their life cycle and keeps a journal of what it did. */
data class GlobalCoachingBaselineService(
    var store: GlobalCoachingBaselineStore,
    var validator: GlobalCoachingBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCoachingBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global coaching baseline $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCoachingBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global coaching baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCoachingBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global coaching baseline $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newGlobalCoachingBaselineService(store: GlobalCoachingBaselineStore, clock: Clock, flags: Registry): GlobalCoachingBaselineService {
    return GlobalCoachingBaselineService(
        store = store,
        validator = defaultGlobalCoachingBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCoachingBaselineEngine(flags),
    )
}
