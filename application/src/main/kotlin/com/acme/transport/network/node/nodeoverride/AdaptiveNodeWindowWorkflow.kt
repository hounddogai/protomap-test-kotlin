package com.acme.transport.network.node.nodeoverride

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves adaptive node windows through their life cycle and keeps a journal of what it did. */
data class AdaptiveNodeWindowCoordinator(
    var store: AdaptiveNodeWindowStore,
    var validator: AdaptiveNodeWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveNodeWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive node window $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveNodeWindowStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive node window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveNodeWindowStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive node window $code not found")
        val result = engine.price(item)
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

fun newAdaptiveNodeWindowCoordinator(store: AdaptiveNodeWindowStore, clock: Clock, flags: Registry): AdaptiveNodeWindowCoordinator {
    return AdaptiveNodeWindowCoordinator(
        store = store,
        validator = defaultAdaptiveNodeWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveNodeWindowEngine(flags),
    )
}
