package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves tiered utilization profiles through their life cycle and keeps a journal of what it did. */
data class TieredUtilizationProfileService(
    var store: TieredUtilizationProfileStore,
    var validator: TieredUtilizationProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered utilization profile $code not found")
        validator.requireValid(item)
        item.moveTo(TieredUtilizationProfileStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered utilization profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredUtilizationProfileStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredUtilizationProfileService(store: TieredUtilizationProfileStore, clock: Clock): TieredUtilizationProfileService {
    return TieredUtilizationProfileService(
        store = store,
        validator = defaultTieredUtilizationProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
