package com.acme.transport.fleetops.utilization.group

import com.acme.transport.fleetops.clock.Clock

/** Moves tiered utilization groups through their life cycle and keeps a journal of what it did. */
data class TieredUtilizationGroupDesk(
    var store: TieredUtilizationGroupStore,
    var validator: TieredUtilizationGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered utilization group $code not found")
        validator.requireValid(item)
        item.moveTo(TieredUtilizationGroupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered utilization group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredUtilizationGroupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTieredUtilizationGroupDesk(store: TieredUtilizationGroupStore, clock: Clock): TieredUtilizationGroupDesk {
    return TieredUtilizationGroupDesk(
        store = store,
        validator = defaultTieredUtilizationGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
