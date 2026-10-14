package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled trailer manifests through their life cycle and keeps a journal of what it did. */
data class ScheduledTrailerManifestManager(
    var store: ScheduledTrailerManifestStore,
    var validator: ScheduledTrailerManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledTrailerManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled trailer manifest $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTrailerManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled trailer manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTrailerManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled trailer manifest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newScheduledTrailerManifestManager(store: ScheduledTrailerManifestStore, clock: Clock): ScheduledTrailerManifestManager {
    return ScheduledTrailerManifestManager(
        store = store,
        validator = defaultScheduledTrailerManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledTrailerManifestEngine(),
    )
}
