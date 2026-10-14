package com.acme.transport.fleetops.utilization.group

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual utilization claims through their life cycle and keeps a journal of what it did. */
data class ManualUtilizationClaimCoordinator(
    var store: ManualUtilizationClaimStore,
    var validator: ManualUtilizationClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualUtilizationClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual utilization claim $code not found")
        validator.requireValid(item)
        item.moveTo(ManualUtilizationClaimStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual utilization claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualUtilizationClaimStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual utilization claim $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newManualUtilizationClaimCoordinator(store: ManualUtilizationClaimStore, clock: Clock, flags: Registry): ManualUtilizationClaimCoordinator {
    return ManualUtilizationClaimCoordinator(
        store = store,
        validator = defaultManualUtilizationClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualUtilizationClaimEngine(flags),
    )
}
