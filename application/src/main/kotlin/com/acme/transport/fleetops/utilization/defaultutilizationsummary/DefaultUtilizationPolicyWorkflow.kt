package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.clock.Clock

/** Moves default utilization policies through their life cycle and keeps a journal of what it did. */
data class DefaultUtilizationPolicyCoordinator(
    var store: DefaultUtilizationPolicyStore,
    var validator: DefaultUtilizationPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default utilization policy $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultUtilizationPolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default utilization policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultUtilizationPolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultUtilizationPolicyCoordinator(store: DefaultUtilizationPolicyStore, clock: Clock): DefaultUtilizationPolicyCoordinator {
    return DefaultUtilizationPolicyCoordinator(
        store = store,
        validator = defaultDefaultUtilizationPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
