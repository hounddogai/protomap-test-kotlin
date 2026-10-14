package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive inspection manifests through their life cycle and keeps a journal of what it did. */
data class AdaptiveInspectionManifestService(
    var store: AdaptiveInspectionManifestStore,
    var validator: AdaptiveInspectionManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive inspection manifest $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveInspectionManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive inspection manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveInspectionManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newAdaptiveInspectionManifestService(store: AdaptiveInspectionManifestStore, clock: Clock): AdaptiveInspectionManifestService {
    return AdaptiveInspectionManifestService(
        store = store,
        validator = defaultAdaptiveInspectionManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
