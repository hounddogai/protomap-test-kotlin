package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization mappings through their life cycle and keeps a journal of what it did. */
data class UtilizationMappingService(
    var store: UtilizationMappingStore,
    var validator: UtilizationMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("utilization mapping $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationMappingStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationMappingStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newUtilizationMappingService(store: UtilizationMappingStore, clock: Clock): UtilizationMappingService {
    return UtilizationMappingService(
        store = store,
        validator = defaultUtilizationMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
