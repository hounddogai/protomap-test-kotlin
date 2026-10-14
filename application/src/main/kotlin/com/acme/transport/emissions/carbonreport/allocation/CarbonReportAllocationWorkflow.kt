package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report allocations through their life cycle and keeps a journal of what it did. */
data class CarbonReportAllocationService(
    var store: CarbonReportAllocationStore,
    var validator: CarbonReportAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("carbon report allocation $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportAllocationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportAllocationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newCarbonReportAllocationService(store: CarbonReportAllocationStore, clock: Clock): CarbonReportAllocationService {
    return CarbonReportAllocationService(
        store = store,
        validator = defaultCarbonReportAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
