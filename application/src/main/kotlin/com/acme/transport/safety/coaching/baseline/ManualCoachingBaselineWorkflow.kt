package com.acme.transport.safety.coaching.baseline

import com.acme.transport.safety.clock.Clock

/** Moves manual coaching baselines through their life cycle and keeps a journal of what it did. */
data class ManualCoachingBaselineService(
    var store: ManualCoachingBaselineStore,
    var validator: ManualCoachingBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual coaching baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCoachingBaselineStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual coaching baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCoachingBaselineStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newManualCoachingBaselineService(store: ManualCoachingBaselineStore, clock: Clock): ManualCoachingBaselineService {
    return ManualCoachingBaselineService(
        store = store,
        validator = defaultManualCoachingBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
