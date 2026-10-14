package com.acme.transport.network.transittime.manifest

import com.acme.transport.network.clock.Clock

/** Moves strict transit time notices through their life cycle and keeps a journal of what it did. */
data class StrictTransitTimeNoticeWorkflow(
    var store: StrictTransitTimeNoticeStore,
    var validator: StrictTransitTimeNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict transit time notice $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTransitTimeNoticeStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict transit time notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTransitTimeNoticeStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newStrictTransitTimeNoticeWorkflow(store: StrictTransitTimeNoticeStore, clock: Clock): StrictTransitTimeNoticeWorkflow {
    return StrictTransitTimeNoticeWorkflow(
        store = store,
        validator = defaultStrictTransitTimeNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
