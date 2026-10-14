package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.crewing.clock.Clock

/** Moves batched duty period forecasts through their life cycle and keeps a journal of what it did. */
data class BatchedDutyPeriodForecastWorkflow(
    var store: BatchedDutyPeriodForecastStore,
    var validator: BatchedDutyPeriodForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDutyPeriodForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched duty period forecast $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDutyPeriodForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched duty period forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDutyPeriodForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched duty period forecast $code not found")
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

fun newBatchedDutyPeriodForecastWorkflow(store: BatchedDutyPeriodForecastStore, clock: Clock): BatchedDutyPeriodForecastWorkflow {
    return BatchedDutyPeriodForecastWorkflow(
        store = store,
        validator = defaultBatchedDutyPeriodForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDutyPeriodForecastEngine(),
    )
}
