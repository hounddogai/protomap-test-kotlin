package com.acme.transport.customs.broker.rollup

import com.acme.transport.customs.clock.Clock

/** Moves broker mappings through their life cycle and keeps a journal of what it did. */
data class BrokerMappingDesk(
    var store: BrokerMappingStore,
    var validator: BrokerMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("broker mapping $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("broker mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerMappingStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newBrokerMappingDesk(store: BrokerMappingStore, clock: Clock): BrokerMappingDesk {
    return BrokerMappingDesk(
        store = store,
        validator = defaultBrokerMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
