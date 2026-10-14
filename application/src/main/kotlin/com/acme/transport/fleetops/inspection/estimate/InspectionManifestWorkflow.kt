package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection manifests through their life cycle and keeps a journal of what it did. */
data class InspectionManifestWorkflow(
    var store: InspectionManifestStore,
    var validator: InspectionManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection manifest $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionManifestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionManifestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newInspectionManifestWorkflow(store: InspectionManifestStore, clock: Clock): InspectionManifestWorkflow {
    return InspectionManifestWorkflow(
        store = store,
        validator = defaultInspectionManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
