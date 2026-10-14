package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.safety.clock.Clock

/** Moves regional incident manifests through their life cycle and keeps a journal of what it did. */
data class RegionalIncidentManifestWorkflow(
    var store: RegionalIncidentManifestStore,
    var validator: RegionalIncidentManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional incident manifest $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalIncidentManifestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional incident manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalIncidentManifestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newRegionalIncidentManifestWorkflow(store: RegionalIncidentManifestStore, clock: Clock): RegionalIncidentManifestWorkflow {
    return RegionalIncidentManifestWorkflow(
        store = store,
        validator = defaultRegionalIncidentManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
