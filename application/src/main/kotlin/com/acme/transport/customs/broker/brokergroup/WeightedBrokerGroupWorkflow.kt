package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.clock.Clock

/** Moves weighted broker groups through their life cycle and keeps a journal of what it did. */
data class WeightedBrokerGroupDesk(
    var store: WeightedBrokerGroupStore,
    var validator: WeightedBrokerGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted broker group $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedBrokerGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted broker group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedBrokerGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newWeightedBrokerGroupDesk(store: WeightedBrokerGroupStore, clock: Clock): WeightedBrokerGroupDesk {
    return WeightedBrokerGroupDesk(
        store = store,
        validator = defaultWeightedBrokerGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
