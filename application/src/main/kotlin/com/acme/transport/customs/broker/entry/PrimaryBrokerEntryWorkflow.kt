package com.acme.transport.customs.broker.entry

import com.acme.transport.customs.clock.Clock

/** Moves primary broker entries through their life cycle and keeps a journal of what it did. */
data class PrimaryBrokerEntryService(
    var store: PrimaryBrokerEntryStore,
    var validator: PrimaryBrokerEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary broker entry $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryBrokerEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary broker entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryBrokerEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newPrimaryBrokerEntryService(store: PrimaryBrokerEntryStore, clock: Clock): PrimaryBrokerEntryService {
    return PrimaryBrokerEntryService(
        store = store,
        validator = defaultPrimaryBrokerEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
