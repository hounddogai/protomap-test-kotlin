package com.acme.transport.linehaul.relay.batch

import com.acme.transport.linehaul.clock.Clock

/** Moves relay segments through their life cycle and keeps a journal of what it did. */
data class RelaySegmentWorkflow(
    var store: RelaySegmentStore,
    var validator: RelaySegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelaySegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("relay segment $code not found")
        validator.requireValid(item)
        item.moveTo(RelaySegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("relay segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelaySegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("relay segment $code not found")
        val result = engine.price(item)
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

fun newRelaySegmentWorkflow(store: RelaySegmentStore, clock: Clock): RelaySegmentWorkflow {
    return RelaySegmentWorkflow(
        store = store,
        validator = defaultRelaySegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelaySegmentEngine(),
    )
}
