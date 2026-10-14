package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.lastmile.clock.Clock

/** Moves attempt manifests through their life cycle and keeps a journal of what it did. */
data class AttemptManifestManager(
    var store: AttemptManifestStore,
    var validator: AttemptManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("attempt manifest $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newAttemptManifestManager(store: AttemptManifestStore, clock: Clock): AttemptManifestManager {
    return AttemptManifestManager(
        store = store,
        validator = defaultAttemptManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
