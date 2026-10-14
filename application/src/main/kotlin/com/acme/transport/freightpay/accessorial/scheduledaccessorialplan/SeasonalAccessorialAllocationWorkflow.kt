package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves seasonal accessorial allocations through their life cycle and keeps a journal of what it did.
 */
data class SeasonalAccessorialAllocationDesk(
    var store: SeasonalAccessorialAllocationStore,
    var validator: SeasonalAccessorialAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal accessorial allocation $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalAccessorialAllocationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal accessorial allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalAccessorialAllocationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newSeasonalAccessorialAllocationDesk(store: SeasonalAccessorialAllocationStore, clock: Clock): SeasonalAccessorialAllocationDesk {
    return SeasonalAccessorialAllocationDesk(
        store = store,
        validator = defaultSeasonalAccessorialAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
