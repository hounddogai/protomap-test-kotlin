package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal redelivery slots through their life cycle and keeps a journal of what it did. */
data class SeasonalRedeliverySlotManager(
    var store: SeasonalRedeliverySlotStore,
    var validator: SeasonalRedeliverySlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal redelivery slot $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRedeliverySlotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal redelivery slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRedeliverySlotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newSeasonalRedeliverySlotManager(store: SeasonalRedeliverySlotStore, clock: Clock): SeasonalRedeliverySlotManager {
    return SeasonalRedeliverySlotManager(
        store = store,
        validator = defaultSeasonalRedeliverySlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
