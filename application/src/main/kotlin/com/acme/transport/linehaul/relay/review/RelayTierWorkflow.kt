package com.acme.transport.linehaul.relay.review

import com.acme.transport.linehaul.clock.Clock

/** Moves relay tiers through their life cycle and keeps a journal of what it did. */
data class RelayTierDesk(
    var store: RelayTierStore,
    var validator: RelayTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("relay tier $code not found")
        validator.requireValid(item)
        item.moveTo(RelayTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("relay tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("relay tier $code not found")
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

fun newRelayTierDesk(store: RelayTierStore, clock: Clock): RelayTierDesk {
    return RelayTierDesk(
        store = store,
        validator = defaultRelayTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayTierEngine(),
    )
}
