package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.clock.Clock

/** Moves batched duty period schedules through their life cycle and keeps a journal of what it did. */
data class BatchedDutyPeriodScheduleManager(
    var store: BatchedDutyPeriodScheduleStore,
    var validator: BatchedDutyPeriodScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDutyPeriodScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched duty period schedule $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDutyPeriodScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched duty period schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDutyPeriodScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched duty period schedule $code not found")
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

fun newBatchedDutyPeriodScheduleManager(store: BatchedDutyPeriodScheduleStore, clock: Clock): BatchedDutyPeriodScheduleManager {
    return BatchedDutyPeriodScheduleManager(
        store = store,
        validator = defaultBatchedDutyPeriodScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDutyPeriodScheduleEngine(),
    )
}
