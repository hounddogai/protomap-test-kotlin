package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled tender batches through their life cycle and keeps a journal of what it did. */
data class ScheduledTenderBatchDesk(
    var store: ScheduledTenderBatchStore,
    var validator: ScheduledTenderBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledTenderBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled tender batch $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTenderBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled tender batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTenderBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled tender batch $code not found")
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

fun newScheduledTenderBatchDesk(store: ScheduledTenderBatchStore, clock: Clock, flags: Registry): ScheduledTenderBatchDesk {
    return ScheduledTenderBatchDesk(
        store = store,
        validator = defaultScheduledTenderBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledTenderBatchEngine(flags),
    )
}
