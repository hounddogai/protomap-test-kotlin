package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves service area proposals through their life cycle and keeps a journal of what it did. */
data class ServiceAreaProposalManager(
    var store: ServiceAreaProposalStore,
    var validator: ServiceAreaProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServiceAreaProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service area proposal $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaProposalStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service area proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaProposalStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("service area proposal $code not found")
        val result = engine.compute(item)
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

fun newServiceAreaProposalManager(store: ServiceAreaProposalStore, clock: Clock, flags: Registry): ServiceAreaProposalManager {
    return ServiceAreaProposalManager(
        store = store,
        validator = defaultServiceAreaProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServiceAreaProposalEngine(flags),
    )
}
