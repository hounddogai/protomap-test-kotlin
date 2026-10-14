package com.acme.transport.safety.scorecard.budget

import com.acme.transport.safety.clock.Clock

/** Moves scorecard manifests through their life cycle and keeps a journal of what it did. */
data class ScorecardManifestCoordinator(
    var store: ScorecardManifestStore,
    var validator: ScorecardManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scorecard manifest $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newScorecardManifestCoordinator(store: ScorecardManifestStore, clock: Clock): ScorecardManifestCoordinator {
    return ScorecardManifestCoordinator(
        store = store,
        validator = defaultScorecardManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
