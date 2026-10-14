package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.clock.Clock

/** Moves broker overrides through their life cycle and keeps a journal of what it did. */
data class BrokerOverrideDesk(
    var store: BrokerOverrideStore,
    var validator: BrokerOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("broker override $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("broker override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newBrokerOverrideDesk(store: BrokerOverrideStore, clock: Clock): BrokerOverrideDesk {
    return BrokerOverrideDesk(
        store = store,
        validator = defaultBrokerOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
