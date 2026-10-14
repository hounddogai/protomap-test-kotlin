package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub bundles through their life cycle and keeps a journal of what it did. */
data class HubBundleCoordinator(
    var store: HubBundleStore,
    var validator: HubBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("hub bundle $code not found")
        validator.requireValid(item)
        item.moveTo(HubBundleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hub bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubBundleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("hub bundle $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newHubBundleCoordinator(store: HubBundleStore, clock: Clock, flags: Registry): HubBundleCoordinator {
    return HubBundleCoordinator(
        store = store,
        validator = defaultHubBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubBundleEngine(flags),
    )
}
