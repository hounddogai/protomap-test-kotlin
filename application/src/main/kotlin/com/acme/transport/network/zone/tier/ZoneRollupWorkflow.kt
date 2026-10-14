package com.acme.transport.network.zone.tier

import com.acme.transport.network.clock.Clock

/** Moves zone rollups through their life cycle and keeps a journal of what it did. */
data class ZoneRollupDesk(
    var store: ZoneRollupStore,
    var validator: ZoneRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("zone rollup $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneRollupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("zone rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneRollupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newZoneRollupDesk(store: ZoneRollupStore, clock: Clock): ZoneRollupDesk {
    return ZoneRollupDesk(
        store = store,
        validator = defaultZoneRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
