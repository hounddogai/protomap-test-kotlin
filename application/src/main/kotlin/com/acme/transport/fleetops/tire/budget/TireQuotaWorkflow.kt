package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.clock.Clock

/** Moves tire quotas through their life cycle and keeps a journal of what it did. */
data class TireQuotaCoordinator(
    var store: TireQuotaStore,
    var validator: TireQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tire quota $code not found")
        validator.requireValid(item)
        item.moveTo(TireQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tire quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireQuotaStatus.STOPPED)
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

fun newTireQuotaCoordinator(store: TireQuotaStore, clock: Clock): TireQuotaCoordinator {
    return TireQuotaCoordinator(
        store = store,
        validator = defaultTireQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
