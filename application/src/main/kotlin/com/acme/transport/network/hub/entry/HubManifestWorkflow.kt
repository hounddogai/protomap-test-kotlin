package com.acme.transport.network.hub.entry

import com.acme.transport.network.clock.Clock

/** Moves hub manifests through their life cycle and keeps a journal of what it did. */
data class HubManifestCoordinator(
    var store: HubManifestStore,
    var validator: HubManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hub manifest $code not found")
        validator.requireValid(item)
        item.moveTo(HubManifestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("hub manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubManifestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("hub manifest $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newHubManifestCoordinator(store: HubManifestStore, clock: Clock): HubManifestCoordinator {
    return HubManifestCoordinator(
        store = store,
        validator = defaultHubManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubManifestEngine(),
    )
}
