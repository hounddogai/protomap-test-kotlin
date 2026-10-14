package com.acme.transport.crewing.swap.draft

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled swap notices through their life cycle and keeps a journal of what it did. */
data class ScheduledSwapNoticeManager(
    var store: ScheduledSwapNoticeStore,
    var validator: ScheduledSwapNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledSwapNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled swap notice $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledSwapNoticeStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled swap notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledSwapNoticeStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled swap notice $code not found")
        val result = engine.price(item)
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

fun newScheduledSwapNoticeManager(store: ScheduledSwapNoticeStore, clock: Clock, flags: Registry): ScheduledSwapNoticeManager {
    return ScheduledSwapNoticeManager(
        store = store,
        validator = defaultScheduledSwapNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledSwapNoticeEngine(flags),
    )
}
