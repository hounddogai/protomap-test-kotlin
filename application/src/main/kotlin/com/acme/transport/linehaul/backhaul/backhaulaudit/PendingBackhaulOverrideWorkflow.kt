package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.linehaul.clock.Clock

/** Moves pending backhaul overrides through their life cycle and keeps a journal of what it did. */
data class PendingBackhaulOverrideWorkflow(
    var store: PendingBackhaulOverrideStore,
    var validator: PendingBackhaulOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingBackhaulOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending backhaul override $code not found")
        validator.requireValid(item)
        item.moveTo(PendingBackhaulOverrideStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending backhaul override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingBackhaulOverrideStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending backhaul override $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newPendingBackhaulOverrideWorkflow(store: PendingBackhaulOverrideStore, clock: Clock): PendingBackhaulOverrideWorkflow {
    return PendingBackhaulOverrideWorkflow(
        store = store,
        validator = defaultPendingBackhaulOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingBackhaulOverrideEngine(),
    )
}
