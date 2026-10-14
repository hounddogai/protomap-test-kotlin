package com.acme.transport.network.zone.zonebundle

import com.acme.transport.network.clock.Clock

/** Moves legacy zone bundles through their life cycle and keeps a journal of what it did. */
data class LegacyZoneBundleService(
    var store: LegacyZoneBundleStore,
    var validator: LegacyZoneBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy zone bundle $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyZoneBundleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy zone bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyZoneBundleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newLegacyZoneBundleService(store: LegacyZoneBundleStore, clock: Clock): LegacyZoneBundleService {
    return LegacyZoneBundleService(
        store = store,
        validator = defaultLegacyZoneBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
