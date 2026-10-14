package com.acme.transport.customs.broker.rollup

import com.acme.transport.customs.clock.Clock

/** Moves broker rollups through their life cycle and keeps a journal of what it did. */
data class BrokerRollupDesk(
    var store: BrokerRollupStore,
    var validator: BrokerRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("broker rollup $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("broker rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newBrokerRollupDesk(store: BrokerRollupStore, clock: Clock): BrokerRollupDesk {
    return BrokerRollupDesk(
        store = store,
        validator = defaultBrokerRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
