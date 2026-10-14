package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.clock.Clock

/** Moves rolling fuel burn requests through their life cycle and keeps a journal of what it did. */
data class RollingFuelBurnRequestWorkflow(
    var store: RollingFuelBurnRequestStore,
    var validator: RollingFuelBurnRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingFuelBurnRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling fuel burn request $code not found")
        validator.requireValid(item)
        item.moveTo(RollingFuelBurnRequestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling fuel burn request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingFuelBurnRequestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling fuel burn request $code not found")
        val result = engine.evaluate(item)
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

fun newRollingFuelBurnRequestWorkflow(store: RollingFuelBurnRequestStore, clock: Clock): RollingFuelBurnRequestWorkflow {
    return RollingFuelBurnRequestWorkflow(
        store = store,
        validator = defaultRollingFuelBurnRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingFuelBurnRequestEngine(),
    )
}
