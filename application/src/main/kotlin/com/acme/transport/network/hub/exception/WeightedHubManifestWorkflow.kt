package com.acme.transport.network.hub.exception

import com.acme.transport.network.clock.Clock

/** Moves weighted hub manifests through their life cycle and keeps a journal of what it did. */
data class WeightedHubManifestCoordinator(
    var store: WeightedHubManifestStore,
    var validator: WeightedHubManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted hub manifest $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedHubManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted hub manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedHubManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newWeightedHubManifestCoordinator(store: WeightedHubManifestStore, clock: Clock): WeightedHubManifestCoordinator {
    return WeightedHubManifestCoordinator(
        store = store,
        validator = defaultWeightedHubManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
