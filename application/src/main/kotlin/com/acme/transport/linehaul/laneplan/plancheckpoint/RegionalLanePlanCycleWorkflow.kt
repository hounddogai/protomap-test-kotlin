package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.linehaul.clock.Clock

/** Moves regional lane plan cycles through their life cycle and keeps a journal of what it did. */
data class RegionalLanePlanCycleDesk(
    var store: RegionalLanePlanCycleStore,
    var validator: RegionalLanePlanCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional lane plan cycle $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalLanePlanCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional lane plan cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalLanePlanCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newRegionalLanePlanCycleDesk(store: RegionalLanePlanCycleStore, clock: Clock): RegionalLanePlanCycleDesk {
    return RegionalLanePlanCycleDesk(
        store = store,
        validator = defaultRegionalLanePlanCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
