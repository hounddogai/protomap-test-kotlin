package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.terminal.clock.Clock

/** Moves manual trailer yard schedules through their life cycle and keeps a journal of what it did. */
data class ManualTrailerYardScheduleWorkflow(
    var store: ManualTrailerYardScheduleStore,
    var validator: ManualTrailerYardScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTrailerYardScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual trailer yard schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTrailerYardScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual trailer yard schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTrailerYardScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual trailer yard schedule $code not found")
        val result = engine.price(item)
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

fun newManualTrailerYardScheduleWorkflow(store: ManualTrailerYardScheduleStore, clock: Clock): ManualTrailerYardScheduleWorkflow {
    return ManualTrailerYardScheduleWorkflow(
        store = store,
        validator = defaultManualTrailerYardScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTrailerYardScheduleEngine(),
    )
}
