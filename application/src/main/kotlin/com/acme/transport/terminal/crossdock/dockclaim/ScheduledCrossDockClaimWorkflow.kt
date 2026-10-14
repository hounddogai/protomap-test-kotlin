package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.terminal.clock.Clock

/** Moves scheduled cross dock claims through their life cycle and keeps a journal of what it did. */
data class ScheduledCrossDockClaimWorkflow(
    var store: ScheduledCrossDockClaimStore,
    var validator: ScheduledCrossDockClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled cross dock claim $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCrossDockClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled cross dock claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCrossDockClaimStatus.RETIRED)
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

fun newScheduledCrossDockClaimWorkflow(store: ScheduledCrossDockClaimStore, clock: Clock): ScheduledCrossDockClaimWorkflow {
    return ScheduledCrossDockClaimWorkflow(
        store = store,
        validator = defaultScheduledCrossDockClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
