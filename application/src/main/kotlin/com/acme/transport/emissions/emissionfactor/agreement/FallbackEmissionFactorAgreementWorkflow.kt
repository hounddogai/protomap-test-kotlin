package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.emissions.clock.Clock

/**
 * Moves fallback emission factor agreements through their life cycle and keeps a journal of what it did.
 */
data class FallbackEmissionFactorAgreementCoordinator(
    var store: FallbackEmissionFactorAgreementStore,
    var validator: FallbackEmissionFactorAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackEmissionFactorAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback emission factor agreement $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEmissionFactorAgreementStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback emission factor agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEmissionFactorAgreementStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback emission factor agreement $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newFallbackEmissionFactorAgreementCoordinator(store: FallbackEmissionFactorAgreementStore, clock: Clock): FallbackEmissionFactorAgreementCoordinator {
    return FallbackEmissionFactorAgreementCoordinator(
        store = store,
        validator = defaultFallbackEmissionFactorAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackEmissionFactorAgreementEngine(),
    )
}
