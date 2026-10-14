package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay profiles through their life cycle and keeps a journal of what it did. */
data class RelayProfileService(
    var store: RelayProfileStore,
    var validator: RelayProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("relay profile $code not found")
        validator.requireValid(item)
        item.moveTo(RelayProfileStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("relay profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayProfileStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("relay profile $code not found")
        val result = engine.compute(item)
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

fun newRelayProfileService(store: RelayProfileStore, clock: Clock, flags: Registry): RelayProfileService {
    return RelayProfileService(
        store = store,
        validator = defaultRelayProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayProfileEngine(flags),
    )
}
