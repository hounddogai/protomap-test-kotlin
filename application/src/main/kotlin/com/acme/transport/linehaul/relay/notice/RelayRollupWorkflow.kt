package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay rollups through their life cycle and keeps a journal of what it did. */
data class RelayRollupDesk(
    var store: RelayRollupStore,
    var validator: RelayRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("relay rollup $code not found")
        validator.requireValid(item)
        item.moveTo(RelayRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("relay rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("relay rollup $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRelayRollupDesk(store: RelayRollupStore, clock: Clock, flags: Registry): RelayRollupDesk {
    return RelayRollupDesk(
        store = store,
        validator = defaultRelayRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayRollupEngine(flags),
    )
}
