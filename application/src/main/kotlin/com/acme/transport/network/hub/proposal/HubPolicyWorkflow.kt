package com.acme.transport.network.hub.proposal

import com.acme.transport.network.clock.Clock

/** Moves hub policies through their life cycle and keeps a journal of what it did. */
data class HubPolicyCoordinator(
    var store: HubPolicyStore,
    var validator: HubPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub policy $code not found")
        validator.requireValid(item)
        item.moveTo(HubPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("hub policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("hub policy $code not found")
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

fun newHubPolicyCoordinator(store: HubPolicyStore, clock: Clock): HubPolicyCoordinator {
    return HubPolicyCoordinator(
        store = store,
        validator = defaultHubPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubPolicyEngine(),
    )
}
