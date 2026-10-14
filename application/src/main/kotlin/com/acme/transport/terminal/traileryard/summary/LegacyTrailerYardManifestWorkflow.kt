package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.terminal.clock.Clock

/** Moves legacy trailer yard manifests through their life cycle and keeps a journal of what it did. */
data class LegacyTrailerYardManifestService(
    var store: LegacyTrailerYardManifestStore,
    var validator: LegacyTrailerYardManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy trailer yard manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTrailerYardManifestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy trailer yard manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTrailerYardManifestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newLegacyTrailerYardManifestService(store: LegacyTrailerYardManifestStore, clock: Clock): LegacyTrailerYardManifestService {
    return LegacyTrailerYardManifestService(
        store = store,
        validator = defaultLegacyTrailerYardManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
