package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled accessorial drafts through their life cycle and keeps a journal of what it did. */
data class ScheduledAccessorialDraftCoordinator(
    var store: ScheduledAccessorialDraftStore,
    var validator: ScheduledAccessorialDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledAccessorialDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled accessorial draft $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAccessorialDraftStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled accessorial draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAccessorialDraftStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled accessorial draft $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newScheduledAccessorialDraftCoordinator(store: ScheduledAccessorialDraftStore, clock: Clock, flags: Registry): ScheduledAccessorialDraftCoordinator {
    return ScheduledAccessorialDraftCoordinator(
        store = store,
        validator = defaultScheduledAccessorialDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledAccessorialDraftEngine(flags),
    )
}
