package com.acme.transport.network.transittime.rollup

import com.acme.transport.network.clock.Clock

/**
 * Moves scheduled transit time manifests through their life cycle and keeps a journal of what it did.
 */
data class ScheduledTransitTimeManifestWorkflow(
    var store: ScheduledTransitTimeManifestStore,
    var validator: ScheduledTransitTimeManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled transit time manifest $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTransitTimeManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled transit time manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTransitTimeManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newScheduledTransitTimeManifestWorkflow(store: ScheduledTransitTimeManifestStore, clock: Clock): ScheduledTransitTimeManifestWorkflow {
    return ScheduledTransitTimeManifestWorkflow(
        store = store,
        validator = defaultScheduledTransitTimeManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
