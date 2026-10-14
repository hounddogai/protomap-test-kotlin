package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge quotas through their life cycle and keeps a journal of what it did. */
data class EvChargeQuotaCoordinator(
    var store: EvChargeQuotaStore,
    var validator: EvChargeQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("ev charge quota $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeQuotaStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeQuotaStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newEvChargeQuotaCoordinator(store: EvChargeQuotaStore, clock: Clock): EvChargeQuotaCoordinator {
    return EvChargeQuotaCoordinator(
        store = store,
        validator = defaultEvChargeQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
