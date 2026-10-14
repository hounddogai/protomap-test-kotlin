package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor proposals through their life cycle and keeps a journal of what it did. */
data class EmissionFactorProposalCoordinator(
    var store: EmissionFactorProposalStore,
    var validator: EmissionFactorProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor proposal $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newEmissionFactorProposalCoordinator(store: EmissionFactorProposalStore, clock: Clock): EmissionFactorProposalCoordinator {
    return EmissionFactorProposalCoordinator(
        store = store,
        validator = defaultEmissionFactorProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
