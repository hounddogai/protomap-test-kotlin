package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.network.clock.Clock

/** Moves regional cutoff claims through their life cycle and keeps a journal of what it did. */
data class RegionalCutoffClaimDesk(
    var store: RegionalCutoffClaimStore,
    var validator: RegionalCutoffClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional cutoff claim $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCutoffClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional cutoff claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCutoffClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newRegionalCutoffClaimDesk(store: RegionalCutoffClaimStore, clock: Clock): RegionalCutoffClaimDesk {
    return RegionalCutoffClaimDesk(
        store = store,
        validator = defaultRegionalCutoffClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
