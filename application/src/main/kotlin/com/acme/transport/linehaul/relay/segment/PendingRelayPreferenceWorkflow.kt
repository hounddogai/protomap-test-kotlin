package com.acme.transport.linehaul.relay.segment

import com.acme.transport.linehaul.clock.Clock

/** Moves pending relay preferences through their life cycle and keeps a journal of what it did. */
data class PendingRelayPreferenceCoordinator(
    var store: PendingRelayPreferenceStore,
    var validator: PendingRelayPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingRelayPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending relay preference $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRelayPreferenceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending relay preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRelayPreferenceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending relay preference $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newPendingRelayPreferenceCoordinator(store: PendingRelayPreferenceStore, clock: Clock): PendingRelayPreferenceCoordinator {
    return PendingRelayPreferenceCoordinator(
        store = store,
        validator = defaultPendingRelayPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingRelayPreferenceEngine(),
    )
}
