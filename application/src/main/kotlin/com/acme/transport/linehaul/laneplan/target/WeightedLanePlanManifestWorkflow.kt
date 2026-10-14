package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.linehaul.clock.Clock

/** Moves weighted lane plan manifests through their life cycle and keeps a journal of what it did. */
data class WeightedLanePlanManifestManager(
    var store: WeightedLanePlanManifestStore,
    var validator: WeightedLanePlanManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted lane plan manifest $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedLanePlanManifestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted lane plan manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedLanePlanManifestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newWeightedLanePlanManifestManager(store: WeightedLanePlanManifestStore, clock: Clock): WeightedLanePlanManifestManager {
    return WeightedLanePlanManifestManager(
        store = store,
        validator = defaultWeightedLanePlanManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
