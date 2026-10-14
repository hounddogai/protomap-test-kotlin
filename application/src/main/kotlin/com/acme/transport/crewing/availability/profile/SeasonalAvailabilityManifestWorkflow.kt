package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.clock.Clock

/**
 * Moves seasonal availability manifests through their life cycle and keeps a journal of what it did.
 */
data class SeasonalAvailabilityManifestDesk(
    var store: SeasonalAvailabilityManifestStore,
    var validator: SeasonalAvailabilityManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal availability manifest $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalAvailabilityManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal availability manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalAvailabilityManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newSeasonalAvailabilityManifestDesk(store: SeasonalAvailabilityManifestStore, clock: Clock): SeasonalAvailabilityManifestDesk {
    return SeasonalAvailabilityManifestDesk(
        store = store,
        validator = defaultSeasonalAvailabilityManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
