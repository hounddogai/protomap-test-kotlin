package com.acme.transport.customs.clearance.notice

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal clearance notices through their life cycle and keeps a journal of what it did. */
data class SeasonalClearanceNoticeCoordinator(
    var store: SeasonalClearanceNoticeStore,
    var validator: SeasonalClearanceNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalClearanceNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal clearance notice $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalClearanceNoticeStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal clearance notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalClearanceNoticeStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal clearance notice $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newSeasonalClearanceNoticeCoordinator(store: SeasonalClearanceNoticeStore, clock: Clock, flags: Registry): SeasonalClearanceNoticeCoordinator {
    return SeasonalClearanceNoticeCoordinator(
        store = store,
        validator = defaultSeasonalClearanceNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalClearanceNoticeEngine(flags),
    )
}
