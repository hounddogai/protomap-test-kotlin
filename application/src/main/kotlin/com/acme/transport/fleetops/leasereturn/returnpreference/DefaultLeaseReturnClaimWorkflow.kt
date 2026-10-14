package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default lease return claims through their life cycle and keeps a journal of what it did. */
data class DefaultLeaseReturnClaimManager(
    var store: DefaultLeaseReturnClaimStore,
    var validator: DefaultLeaseReturnClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultLeaseReturnClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default lease return claim $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultLeaseReturnClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default lease return claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultLeaseReturnClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default lease return claim $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultLeaseReturnClaimManager(store: DefaultLeaseReturnClaimStore, clock: Clock, flags: Registry): DefaultLeaseReturnClaimManager {
    return DefaultLeaseReturnClaimManager(
        store = store,
        validator = defaultDefaultLeaseReturnClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultLeaseReturnClaimEngine(flags),
    )
}
