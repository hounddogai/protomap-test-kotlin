package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.network.clock.Clock

/** Moves rolling cutoff preferences through their life cycle and keeps a journal of what it did. */
data class RollingCutoffPreferenceService(
    var store: RollingCutoffPreferenceStore,
    var validator: RollingCutoffPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling cutoff preference $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCutoffPreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling cutoff preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCutoffPreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRollingCutoffPreferenceService(store: RollingCutoffPreferenceStore, clock: Clock): RollingCutoffPreferenceService {
    return RollingCutoffPreferenceService(
        store = store,
        validator = defaultRollingCutoffPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
