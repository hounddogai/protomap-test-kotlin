package com.acme.transport.safety.coaching.metric

import com.acme.transport.safety.clock.Clock

/** Moves coaching quotas through their life cycle and keeps a journal of what it did. */
data class CoachingQuotaManager(
    var store: CoachingQuotaStore,
    var validator: CoachingQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("coaching quota $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingQuotaStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingQuotaStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newCoachingQuotaManager(store: CoachingQuotaStore, clock: Clock): CoachingQuotaManager {
    return CoachingQuotaManager(
        store = store,
        validator = defaultCoachingQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
