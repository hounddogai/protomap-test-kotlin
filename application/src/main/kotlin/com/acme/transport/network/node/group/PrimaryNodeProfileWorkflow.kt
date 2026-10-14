package com.acme.transport.network.node.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves primary node profiles through their life cycle and keeps a journal of what it did. */
data class PrimaryNodeProfileWorkflow(
    var store: PrimaryNodeProfileStore,
    var validator: PrimaryNodeProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryNodeProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary node profile $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryNodeProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary node profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryNodeProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary node profile $code not found")
        val result = engine.price(item)
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

fun newPrimaryNodeProfileWorkflow(store: PrimaryNodeProfileStore, clock: Clock, flags: Registry): PrimaryNodeProfileWorkflow {
    return PrimaryNodeProfileWorkflow(
        store = store,
        validator = defaultPrimaryNodeProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryNodeProfileEngine(flags),
    )
}
