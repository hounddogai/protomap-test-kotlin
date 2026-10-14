package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.clock.Clock

/** Moves rolling idling requests through their life cycle and keeps a journal of what it did. */
data class RollingIdlingRequestWorkflow(
    var store: RollingIdlingRequestStore,
    var validator: RollingIdlingRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling idling request $code not found")
        validator.requireValid(item)
        item.moveTo(RollingIdlingRequestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling idling request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingIdlingRequestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newRollingIdlingRequestWorkflow(store: RollingIdlingRequestStore, clock: Clock): RollingIdlingRequestWorkflow {
    return RollingIdlingRequestWorkflow(
        store = store,
        validator = defaultRollingIdlingRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
