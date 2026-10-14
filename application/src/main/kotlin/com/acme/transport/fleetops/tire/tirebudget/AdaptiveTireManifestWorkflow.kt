package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive tire manifests through their life cycle and keeps a journal of what it did. */
data class AdaptiveTireManifestManager(
    var store: AdaptiveTireManifestStore,
    var validator: AdaptiveTireManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive tire manifest $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTireManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive tire manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTireManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newAdaptiveTireManifestManager(store: AdaptiveTireManifestStore, clock: Clock): AdaptiveTireManifestManager {
    return AdaptiveTireManifestManager(
        store = store,
        validator = defaultAdaptiveTireManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
