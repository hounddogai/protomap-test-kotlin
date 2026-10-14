package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.emissions.clock.Clock

/**
 * Moves pending emission factor overrides through their life cycle and keeps a journal of what it did.
 */
data class PendingEmissionFactorOverrideDesk(
    var store: PendingEmissionFactorOverrideStore,
    var validator: PendingEmissionFactorOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending emission factor override $code not found")
        validator.requireValid(item)
        item.moveTo(PendingEmissionFactorOverrideStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending emission factor override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingEmissionFactorOverrideStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPendingEmissionFactorOverrideDesk(store: PendingEmissionFactorOverrideStore, clock: Clock): PendingEmissionFactorOverrideDesk {
    return PendingEmissionFactorOverrideDesk(
        store = store,
        validator = defaultPendingEmissionFactorOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
