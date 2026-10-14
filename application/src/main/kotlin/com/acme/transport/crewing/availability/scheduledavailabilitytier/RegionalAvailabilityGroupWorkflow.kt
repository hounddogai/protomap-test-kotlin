package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.crewing.clock.Clock

/** Moves regional availability groups through their life cycle and keeps a journal of what it did. */
data class RegionalAvailabilityGroupManager(
    var store: RegionalAvailabilityGroupStore,
    var validator: RegionalAvailabilityGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional availability group $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalAvailabilityGroupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional availability group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalAvailabilityGroupStatus.RETIRED)
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

fun newRegionalAvailabilityGroupManager(store: RegionalAvailabilityGroupStore, clock: Clock): RegionalAvailabilityGroupManager {
    return RegionalAvailabilityGroupManager(
        store = store,
        validator = defaultRegionalAvailabilityGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
