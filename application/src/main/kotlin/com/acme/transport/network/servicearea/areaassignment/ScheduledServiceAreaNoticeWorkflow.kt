package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves scheduled service area notices through their life cycle and keeps a journal of what it did.
 */
data class ScheduledServiceAreaNoticeService(
    var store: ScheduledServiceAreaNoticeStore,
    var validator: ScheduledServiceAreaNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledServiceAreaNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled service area notice $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledServiceAreaNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled service area notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledServiceAreaNoticeStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled service area notice $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newScheduledServiceAreaNoticeService(store: ScheduledServiceAreaNoticeStore, clock: Clock, flags: Registry): ScheduledServiceAreaNoticeService {
    return ScheduledServiceAreaNoticeService(
        store = store,
        validator = defaultScheduledServiceAreaNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledServiceAreaNoticeEngine(flags),
    )
}
