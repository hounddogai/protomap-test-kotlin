package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.clock.Clock

/** Moves broker profiles through their life cycle and keeps a journal of what it did. */
data class BrokerProfileCoordinator(
    var store: BrokerProfileStore,
    var validator: BrokerProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("broker profile $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerProfileStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("broker profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerProfileStatus.CANCELLED)
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

fun newBrokerProfileCoordinator(store: BrokerProfileStore, clock: Clock): BrokerProfileCoordinator {
    return BrokerProfileCoordinator(
        store = store,
        validator = defaultBrokerProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
