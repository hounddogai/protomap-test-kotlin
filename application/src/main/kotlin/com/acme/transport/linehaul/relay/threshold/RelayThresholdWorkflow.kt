package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.linehaul.clock.Clock

/** Moves relay thresholds through their life cycle and keeps a journal of what it did. */
data class RelayThresholdWorkflow(
    var store: RelayThresholdStore,
    var validator: RelayThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("relay threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RelayThresholdStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("relay threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayThresholdStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("relay threshold $code not found")
        val result = engine.run(item)
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

fun newRelayThresholdWorkflow(store: RelayThresholdStore, clock: Clock): RelayThresholdWorkflow {
    return RelayThresholdWorkflow(
        store = store,
        validator = defaultRelayThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayThresholdEngine(),
    )
}
