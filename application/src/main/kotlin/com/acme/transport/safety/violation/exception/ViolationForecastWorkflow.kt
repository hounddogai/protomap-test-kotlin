package com.acme.transport.safety.violation.exception

import com.acme.transport.safety.clock.Clock

/** Moves violation forecasts through their life cycle and keeps a journal of what it did. */
data class ViolationForecastWorkflow(
    var store: ViolationForecastStore,
    var validator: ViolationForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("violation forecast $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationForecastStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("violation forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationForecastStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newViolationForecastWorkflow(store: ViolationForecastStore, clock: Clock): ViolationForecastWorkflow {
    return ViolationForecastWorkflow(
        store = store,
        validator = defaultViolationForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
