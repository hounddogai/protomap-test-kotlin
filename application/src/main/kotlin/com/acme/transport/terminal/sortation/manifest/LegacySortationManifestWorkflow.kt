package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.terminal.clock.Clock

/** Moves legacy sortation manifests through their life cycle and keeps a journal of what it did. */
data class LegacySortationManifestManager(
    var store: LegacySortationManifestStore,
    var validator: LegacySortationManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy sortation manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LegacySortationManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy sortation manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacySortationManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLegacySortationManifestManager(store: LegacySortationManifestStore, clock: Clock): LegacySortationManifestManager {
    return LegacySortationManifestManager(
        store = store,
        validator = defaultLegacySortationManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
