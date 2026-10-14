package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.customs.clock.Clock

/** Moves lenient broker agreements through their life cycle and keeps a journal of what it did. */
data class LenientBrokerAgreementService(
    var store: LenientBrokerAgreementStore,
    var validator: LenientBrokerAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient broker agreement $code not found")
        validator.requireValid(item)
        item.moveTo(LenientBrokerAgreementStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient broker agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientBrokerAgreementStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientBrokerAgreementService(store: LenientBrokerAgreementStore, clock: Clock): LenientBrokerAgreementService {
    return LenientBrokerAgreementService(
        store = store,
        validator = defaultLenientBrokerAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
