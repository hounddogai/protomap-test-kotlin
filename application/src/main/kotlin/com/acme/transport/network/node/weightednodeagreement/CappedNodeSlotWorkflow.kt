package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves capped node slots through their life cycle and keeps a journal of what it did. */
data class CappedNodeSlotManager(
    var store: CappedNodeSlotStore,
    var validator: CappedNodeSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedNodeSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped node slot $code not found")
        validator.requireValid(item)
        item.moveTo(CappedNodeSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped node slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedNodeSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped node slot $code not found")
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

fun newCappedNodeSlotManager(store: CappedNodeSlotStore, clock: Clock, flags: Registry): CappedNodeSlotManager {
    return CappedNodeSlotManager(
        store = store,
        validator = defaultCappedNodeSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedNodeSlotEngine(flags),
    )
}
