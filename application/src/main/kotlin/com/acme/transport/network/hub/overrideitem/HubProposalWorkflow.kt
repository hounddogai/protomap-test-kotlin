package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub proposals through their life cycle and keeps a journal of what it did. */
data class HubProposalCoordinator(
    var store: HubProposalStore,
    var validator: HubProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hub proposal $code not found")
        validator.requireValid(item)
        item.moveTo(HubProposalStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubProposalStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("hub proposal $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newHubProposalCoordinator(store: HubProposalStore, clock: Clock, flags: Registry): HubProposalCoordinator {
    return HubProposalCoordinator(
        store = store,
        validator = defaultHubProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubProposalEngine(flags),
    )
}
