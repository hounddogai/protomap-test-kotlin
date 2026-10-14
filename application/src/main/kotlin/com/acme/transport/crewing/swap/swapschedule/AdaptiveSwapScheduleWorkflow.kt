package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.clock.Clock

/** Moves adaptive swap schedules through their life cycle and keeps a journal of what it did. */
data class AdaptiveSwapScheduleWorkflow(
    var store: AdaptiveSwapScheduleStore,
    var validator: AdaptiveSwapScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveSwapScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive swap schedule $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveSwapScheduleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive swap schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveSwapScheduleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive swap schedule $code not found")
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

fun newAdaptiveSwapScheduleWorkflow(store: AdaptiveSwapScheduleStore, clock: Clock): AdaptiveSwapScheduleWorkflow {
    return AdaptiveSwapScheduleWorkflow(
        store = store,
        validator = defaultAdaptiveSwapScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveSwapScheduleEngine(),
    )
}
