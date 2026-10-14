package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled accessorial batches through their life cycle and keeps a journal of what it did. */
data class ScheduledAccessorialBatchDesk(
    var store: ScheduledAccessorialBatchStore,
    var validator: ScheduledAccessorialBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledAccessorialBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled accessorial batch $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAccessorialBatchStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled accessorial batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAccessorialBatchStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled accessorial batch $code not found")
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

fun newScheduledAccessorialBatchDesk(store: ScheduledAccessorialBatchStore, clock: Clock, flags: Registry): ScheduledAccessorialBatchDesk {
    return ScheduledAccessorialBatchDesk(
        store = store,
        validator = defaultScheduledAccessorialBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledAccessorialBatchEngine(flags),
    )
}
