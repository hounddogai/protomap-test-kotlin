package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves attempt schedules through their life cycle and keeps a journal of what it did. */
data class AttemptScheduleWorkflow(
    var store: AttemptScheduleStore,
    var validator: AttemptScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AttemptScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("attempt schedule $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptScheduleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptScheduleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("attempt schedule $code not found")
        val result = engine.run(item)
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

fun newAttemptScheduleWorkflow(store: AttemptScheduleStore, clock: Clock, flags: Registry): AttemptScheduleWorkflow {
    return AttemptScheduleWorkflow(
        store = store,
        validator = defaultAttemptScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAttemptScheduleEngine(flags),
    )
}
