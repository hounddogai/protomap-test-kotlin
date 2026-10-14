package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves capped utilization preferences through their life cycle and keeps a journal of what it did.
 */
data class CappedUtilizationPreferenceManager(
    var store: CappedUtilizationPreferenceStore,
    var validator: CappedUtilizationPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedUtilizationPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped utilization preference $code not found")
        validator.requireValid(item)
        item.moveTo(CappedUtilizationPreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped utilization preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedUtilizationPreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped utilization preference $code not found")
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

fun newCappedUtilizationPreferenceManager(store: CappedUtilizationPreferenceStore, clock: Clock, flags: Registry): CappedUtilizationPreferenceManager {
    return CappedUtilizationPreferenceManager(
        store = store,
        validator = defaultCappedUtilizationPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedUtilizationPreferenceEngine(flags),
    )
}
