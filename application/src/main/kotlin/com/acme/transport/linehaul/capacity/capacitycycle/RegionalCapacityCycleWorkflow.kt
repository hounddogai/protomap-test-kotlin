package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.linehaul.clock.Clock

/** Moves regional capacity cycles through their life cycle and keeps a journal of what it did. */
data class RegionalCapacityCycleService(
    var store: RegionalCapacityCycleStore,
    var validator: RegionalCapacityCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional capacity cycle $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCapacityCycleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional capacity cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCapacityCycleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newRegionalCapacityCycleService(store: RegionalCapacityCycleStore, clock: Clock): RegionalCapacityCycleService {
    return RegionalCapacityCycleService(
        store = store,
        validator = defaultRegionalCapacityCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
