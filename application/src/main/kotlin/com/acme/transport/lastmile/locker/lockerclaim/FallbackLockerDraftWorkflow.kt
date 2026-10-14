package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves fallback locker drafts through their life cycle and keeps a journal of what it did. */
data class FallbackLockerDraftManager(
    var store: FallbackLockerDraftStore,
    var validator: FallbackLockerDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackLockerDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback locker draft $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackLockerDraftStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback locker draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackLockerDraftStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback locker draft $code not found")
        val result = engine.compute(item)
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

fun newFallbackLockerDraftManager(store: FallbackLockerDraftStore, clock: Clock, flags: Registry): FallbackLockerDraftManager {
    return FallbackLockerDraftManager(
        store = store,
        validator = defaultFallbackLockerDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackLockerDraftEngine(flags),
    )
}
