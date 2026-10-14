package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.customs.clock.Clock

/** Moves pending clearance overrides through their life cycle and keeps a journal of what it did. */
data class PendingClearanceOverrideWorkflow(
    var store: PendingClearanceOverrideStore,
    var validator: PendingClearanceOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingClearanceOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending clearance override $code not found")
        validator.requireValid(item)
        item.moveTo(PendingClearanceOverrideStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending clearance override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingClearanceOverrideStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending clearance override $code not found")
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

fun newPendingClearanceOverrideWorkflow(store: PendingClearanceOverrideStore, clock: Clock): PendingClearanceOverrideWorkflow {
    return PendingClearanceOverrideWorkflow(
        store = store,
        validator = defaultPendingClearanceOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingClearanceOverrideEngine(),
    )
}
