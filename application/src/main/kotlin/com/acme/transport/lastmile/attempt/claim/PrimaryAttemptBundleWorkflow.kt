package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.lastmile.clock.Clock

/** Moves primary attempt bundles through their life cycle and keeps a journal of what it did. */
data class PrimaryAttemptBundleService(
    var store: PrimaryAttemptBundleStore,
    var validator: PrimaryAttemptBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary attempt bundle $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAttemptBundleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary attempt bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAttemptBundleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newPrimaryAttemptBundleService(store: PrimaryAttemptBundleStore, clock: Clock): PrimaryAttemptBundleService {
    return PrimaryAttemptBundleService(
        store = store,
        validator = defaultPrimaryAttemptBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
