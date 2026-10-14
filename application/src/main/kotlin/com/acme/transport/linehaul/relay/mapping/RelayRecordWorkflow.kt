package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.linehaul.clock.Clock

/** Moves relay records through their life cycle and keeps a journal of what it did. */
data class RelayRecordWorkflow(
    var store: RelayRecordStore,
    var validator: RelayRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("relay record $code not found")
        validator.requireValid(item)
        item.moveTo(RelayRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("relay record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("relay record $code not found")
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

fun newRelayRecordWorkflow(store: RelayRecordStore, clock: Clock): RelayRecordWorkflow {
    return RelayRecordWorkflow(
        store = store,
        validator = defaultRelayRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayRecordEngine(),
    )
}
