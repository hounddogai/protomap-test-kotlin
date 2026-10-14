package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay overrides through their life cycle and keeps a journal of what it did. */
data class RelayOverrideDesk(
    var store: RelayOverrideStore,
    var validator: RelayOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("relay override $code not found")
        validator.requireValid(item)
        item.moveTo(RelayOverrideStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("relay override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayOverrideStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("relay override $code not found")
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

fun newRelayOverrideDesk(store: RelayOverrideStore, clock: Clock, flags: Registry): RelayOverrideDesk {
    return RelayOverrideDesk(
        store = store,
        validator = defaultRelayOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayOverrideEngine(flags),
    )
}
