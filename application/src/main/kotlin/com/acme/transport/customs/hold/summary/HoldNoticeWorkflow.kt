package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves hold notices through their life cycle and keeps a journal of what it did. */
data class HoldNoticeCoordinator(
    var store: HoldNoticeStore,
    var validator: HoldNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HoldNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hold notice $code not found")
        validator.requireValid(item)
        item.moveTo(HoldNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hold notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("hold notice $code not found")
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

fun newHoldNoticeCoordinator(store: HoldNoticeStore, clock: Clock, flags: Registry): HoldNoticeCoordinator {
    return HoldNoticeCoordinator(
        store = store,
        validator = defaultHoldNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHoldNoticeEngine(flags),
    )
}
