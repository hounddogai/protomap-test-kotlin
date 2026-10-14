package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves capped transit time notices through their life cycle and keeps a journal of what it did. */
data class CappedTransitTimeNoticeCoordinator(
    var store: CappedTransitTimeNoticeStore,
    var validator: CappedTransitTimeNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTransitTimeNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped transit time notice $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTransitTimeNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped transit time notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTransitTimeNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped transit time notice $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newCappedTransitTimeNoticeCoordinator(store: CappedTransitTimeNoticeStore, clock: Clock, flags: Registry): CappedTransitTimeNoticeCoordinator {
    return CappedTransitTimeNoticeCoordinator(
        store = store,
        validator = defaultCappedTransitTimeNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTransitTimeNoticeEngine(flags),
    )
}
