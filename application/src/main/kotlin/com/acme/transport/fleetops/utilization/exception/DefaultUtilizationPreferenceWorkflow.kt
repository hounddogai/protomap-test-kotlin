package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves default utilization preferences through their life cycle and keeps a journal of what it did.
 */
data class DefaultUtilizationPreferenceService(
    var store: DefaultUtilizationPreferenceStore,
    var validator: DefaultUtilizationPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default utilization preference $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultUtilizationPreferenceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default utilization preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultUtilizationPreferenceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newDefaultUtilizationPreferenceService(store: DefaultUtilizationPreferenceStore, clock: Clock): DefaultUtilizationPreferenceService {
    return DefaultUtilizationPreferenceService(
        store = store,
        validator = defaultDefaultUtilizationPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
