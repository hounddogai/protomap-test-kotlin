package com.acme.transport.customs.broker.agreement

import com.acme.transport.customs.clock.Clock

/** Moves broker agreements through their life cycle and keeps a journal of what it did. */
data class BrokerAgreementManager(
    var store: BrokerAgreementStore,
    var validator: BrokerAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("broker agreement $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("broker agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newBrokerAgreementManager(store: BrokerAgreementStore, clock: Clock): BrokerAgreementManager {
    return BrokerAgreementManager(
        store = store,
        validator = defaultBrokerAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
