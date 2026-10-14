package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.linehaul.clock.Clock

/** Moves rolling backhaul policies through their life cycle and keeps a journal of what it did. */
data class RollingBackhaulPolicyCoordinator(
    var store: RollingBackhaulPolicyStore,
    var validator: RollingBackhaulPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling backhaul policy $code not found")
        validator.requireValid(item)
        item.moveTo(RollingBackhaulPolicyStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling backhaul policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingBackhaulPolicyStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newRollingBackhaulPolicyCoordinator(store: RollingBackhaulPolicyStore, clock: Clock): RollingBackhaulPolicyCoordinator {
    return RollingBackhaulPolicyCoordinator(
        store = store,
        validator = defaultRollingBackhaulPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
