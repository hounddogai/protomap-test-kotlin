package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.clock.Clock

/**
 * Moves regional emission factor claims through their life cycle and keeps a journal of what it did.
 */
data class RegionalEmissionFactorClaimManager(
    var store: RegionalEmissionFactorClaimStore,
    var validator: RegionalEmissionFactorClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional emission factor claim $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalEmissionFactorClaimStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional emission factor claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalEmissionFactorClaimStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newRegionalEmissionFactorClaimManager(store: RegionalEmissionFactorClaimStore, clock: Clock): RegionalEmissionFactorClaimManager {
    return RegionalEmissionFactorClaimManager(
        store = store,
        validator = defaultRegionalEmissionFactorClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
