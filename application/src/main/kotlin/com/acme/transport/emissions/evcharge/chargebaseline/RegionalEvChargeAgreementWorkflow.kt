package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.emissions.clock.Clock

/** Moves regional ev charge agreements through their life cycle and keeps a journal of what it did. */
data class RegionalEvChargeAgreementCoordinator(
    var store: RegionalEvChargeAgreementStore,
    var validator: RegionalEvChargeAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional ev charge agreement $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalEvChargeAgreementStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional ev charge agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalEvChargeAgreementStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRegionalEvChargeAgreementCoordinator(store: RegionalEvChargeAgreementStore, clock: Clock): RegionalEvChargeAgreementCoordinator {
    return RegionalEvChargeAgreementCoordinator(
        store = store,
        validator = defaultRegionalEvChargeAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
