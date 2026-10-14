package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.clock.Clock

/**
 * Moves capped emission factor proposals through their life cycle and keeps a journal of what it did.
 */
data class CappedEmissionFactorProposalService(
    var store: CappedEmissionFactorProposalStore,
    var validator: CappedEmissionFactorProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedEmissionFactorProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped emission factor proposal $code not found")
        validator.requireValid(item)
        item.moveTo(CappedEmissionFactorProposalStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped emission factor proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedEmissionFactorProposalStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped emission factor proposal $code not found")
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

fun newCappedEmissionFactorProposalService(store: CappedEmissionFactorProposalStore, clock: Clock): CappedEmissionFactorProposalService {
    return CappedEmissionFactorProposalService(
        store = store,
        validator = defaultCappedEmissionFactorProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedEmissionFactorProposalEngine(),
    )
}
