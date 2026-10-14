package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.clock.Clock

/** Moves default clearance targets through their life cycle and keeps a journal of what it did. */
data class DefaultClearanceTargetWorkflow(
    var store: DefaultClearanceTargetStore,
    var validator: DefaultClearanceTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultClearanceTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default clearance target $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultClearanceTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default clearance target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultClearanceTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default clearance target $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newDefaultClearanceTargetWorkflow(store: DefaultClearanceTargetStore, clock: Clock): DefaultClearanceTargetWorkflow {
    return DefaultClearanceTargetWorkflow(
        store = store,
        validator = defaultDefaultClearanceTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultClearanceTargetEngine(),
    )
}
