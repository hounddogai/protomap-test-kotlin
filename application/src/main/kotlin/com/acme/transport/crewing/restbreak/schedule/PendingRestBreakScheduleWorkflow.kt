package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending rest break schedules through their life cycle and keeps a journal of what it did. */
data class PendingRestBreakScheduleWorkflow(
    var store: PendingRestBreakScheduleStore,
    var validator: PendingRestBreakScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingRestBreakScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending rest break schedule $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRestBreakScheduleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending rest break schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRestBreakScheduleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending rest break schedule $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newPendingRestBreakScheduleWorkflow(store: PendingRestBreakScheduleStore, clock: Clock, flags: Registry): PendingRestBreakScheduleWorkflow {
    return PendingRestBreakScheduleWorkflow(
        store = store,
        validator = defaultPendingRestBreakScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingRestBreakScheduleEngine(flags),
    )
}
