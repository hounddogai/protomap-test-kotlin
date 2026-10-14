package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.clock.Clock

/** Moves regional broker checkpoints through their life cycle and keeps a journal of what it did. */
data class RegionalBrokerCheckpointWorkflow(
    var store: RegionalBrokerCheckpointStore,
    var validator: RegionalBrokerCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalBrokerCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional broker checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalBrokerCheckpointStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional broker checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalBrokerCheckpointStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional broker checkpoint $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newRegionalBrokerCheckpointWorkflow(store: RegionalBrokerCheckpointStore, clock: Clock): RegionalBrokerCheckpointWorkflow {
    return RegionalBrokerCheckpointWorkflow(
        store = store,
        validator = defaultRegionalBrokerCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalBrokerCheckpointEngine(),
    )
}
