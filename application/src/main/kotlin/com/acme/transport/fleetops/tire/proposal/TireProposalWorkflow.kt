package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire proposals through their life cycle and keeps a journal of what it did. */
data class TireProposalCoordinator(
    var store: TireProposalStore,
    var validator: TireProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tire proposal $code not found")
        validator.requireValid(item)
        item.moveTo(TireProposalStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tire proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireProposalStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tire proposal $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newTireProposalCoordinator(store: TireProposalStore, clock: Clock, flags: Registry): TireProposalCoordinator {
    return TireProposalCoordinator(
        store = store,
        validator = defaultTireProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireProposalEngine(flags),
    )
}
