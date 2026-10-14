package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul drafts through their life cycle and keeps a journal of what it did. */
data class BackhaulDraftCoordinator(
    var store: BackhaulDraftStore,
    var validator: BackhaulDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("backhaul draft $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulDraftStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulDraftStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("backhaul draft $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newBackhaulDraftCoordinator(store: BackhaulDraftStore, clock: Clock, flags: Registry): BackhaulDraftCoordinator {
    return BackhaulDraftCoordinator(
        store = store,
        validator = defaultBackhaulDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulDraftEngine(flags),
    )
}
