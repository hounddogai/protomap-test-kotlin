package com.acme.transport.network.transittime.digest

import com.acme.transport.network.clock.Clock

/** Moves transit time manifests through their life cycle and keeps a journal of what it did. */
data class TransitTimeManifestCoordinator(
    var store: TransitTimeManifestStore,
    var validator: TransitTimeManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("transit time manifest $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeManifestStatus.FAILED)
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

fun newTransitTimeManifestCoordinator(store: TransitTimeManifestStore, clock: Clock): TransitTimeManifestCoordinator {
    return TransitTimeManifestCoordinator(
        store = store,
        validator = defaultTransitTimeManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
