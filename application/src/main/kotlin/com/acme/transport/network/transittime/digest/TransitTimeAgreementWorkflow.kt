package com.acme.transport.network.transittime.digest

import com.acme.transport.network.clock.Clock

/** Moves transit time agreements through their life cycle and keeps a journal of what it did. */
data class TransitTimeAgreementManager(
    var store: TransitTimeAgreementStore,
    var validator: TransitTimeAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("transit time agreement $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeAgreementStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeAgreementStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newTransitTimeAgreementManager(store: TransitTimeAgreementStore, clock: Clock): TransitTimeAgreementManager {
    return TransitTimeAgreementManager(
        store = store,
        validator = defaultTransitTimeAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
