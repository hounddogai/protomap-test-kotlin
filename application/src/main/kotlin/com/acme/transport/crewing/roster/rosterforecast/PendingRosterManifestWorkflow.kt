package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.clock.Clock

/** Moves pending roster manifests through their life cycle and keeps a journal of what it did. */
data class PendingRosterManifestWorkflow(
    var store: PendingRosterManifestStore,
    var validator: PendingRosterManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingRosterManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending roster manifest $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRosterManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending roster manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRosterManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending roster manifest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingRosterManifestWorkflow(store: PendingRosterManifestStore, clock: Clock): PendingRosterManifestWorkflow {
    return PendingRosterManifestWorkflow(
        store = store,
        validator = defaultPendingRosterManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingRosterManifestEngine(),
    )
}
