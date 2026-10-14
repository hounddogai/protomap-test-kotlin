package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves rolling emission factor agreements through their life cycle and keeps a journal of what it did.
 */
data class RollingEmissionFactorAgreementWorkflow(
    var store: RollingEmissionFactorAgreementStore,
    var validator: RollingEmissionFactorAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingEmissionFactorAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling emission factor agreement $code not found")
        validator.requireValid(item)
        item.moveTo(RollingEmissionFactorAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling emission factor agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingEmissionFactorAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling emission factor agreement $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newRollingEmissionFactorAgreementWorkflow(store: RollingEmissionFactorAgreementStore, clock: Clock, flags: Registry): RollingEmissionFactorAgreementWorkflow {
    return RollingEmissionFactorAgreementWorkflow(
        store = store,
        validator = defaultRollingEmissionFactorAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingEmissionFactorAgreementEngine(flags),
    )
}
