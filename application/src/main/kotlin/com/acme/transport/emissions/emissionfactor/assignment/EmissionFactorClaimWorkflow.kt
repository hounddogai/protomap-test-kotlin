package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor claims through their life cycle and keeps a journal of what it did. */
data class EmissionFactorClaimService(
    var store: EmissionFactorClaimStore,
    var validator: EmissionFactorClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("emission factor claim $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorClaimStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorClaimStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newEmissionFactorClaimService(store: EmissionFactorClaimStore, clock: Clock): EmissionFactorClaimService {
    return EmissionFactorClaimService(
        store = store,
        validator = defaultEmissionFactorClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
