package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return overrides through their life cycle and keeps a journal of what it did. */
data class LeaseReturnOverrideCoordinator(
    var store: LeaseReturnOverrideStore,
    var validator: LeaseReturnOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lease return override $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnOverrideStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnOverrideStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lease return override $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newLeaseReturnOverrideCoordinator(store: LeaseReturnOverrideStore, clock: Clock): LeaseReturnOverrideCoordinator {
    return LeaseReturnOverrideCoordinator(
        store = store,
        validator = defaultLeaseReturnOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnOverrideEngine(),
    )
}
