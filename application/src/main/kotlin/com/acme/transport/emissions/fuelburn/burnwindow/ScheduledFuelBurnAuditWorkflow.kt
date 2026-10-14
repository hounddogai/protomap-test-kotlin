package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.clock.Clock

/** Moves scheduled fuel burn audits through their life cycle and keeps a journal of what it did. */
data class ScheduledFuelBurnAuditManager(
    var store: ScheduledFuelBurnAuditStore,
    var validator: ScheduledFuelBurnAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled fuel burn audit $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledFuelBurnAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled fuel burn audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledFuelBurnAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newScheduledFuelBurnAuditManager(store: ScheduledFuelBurnAuditStore, clock: Clock): ScheduledFuelBurnAuditManager {
    return ScheduledFuelBurnAuditManager(
        store = store,
        validator = defaultScheduledFuelBurnAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
