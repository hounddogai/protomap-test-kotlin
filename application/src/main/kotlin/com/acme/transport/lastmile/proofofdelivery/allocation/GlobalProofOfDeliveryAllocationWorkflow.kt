package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves global proof of delivery allocations through their life cycle and keeps a journal of what it did.
 */
data class GlobalProofOfDeliveryAllocationCoordinator(
    var store: GlobalProofOfDeliveryAllocationStore,
    var validator: GlobalProofOfDeliveryAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalProofOfDeliveryAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global proof of delivery allocation $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalProofOfDeliveryAllocationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global proof of delivery allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalProofOfDeliveryAllocationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global proof of delivery allocation $code not found")
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

fun newGlobalProofOfDeliveryAllocationCoordinator(store: GlobalProofOfDeliveryAllocationStore, clock: Clock, flags: Registry): GlobalProofOfDeliveryAllocationCoordinator {
    return GlobalProofOfDeliveryAllocationCoordinator(
        store = store,
        validator = defaultGlobalProofOfDeliveryAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalProofOfDeliveryAllocationEngine(flags),
    )
}
