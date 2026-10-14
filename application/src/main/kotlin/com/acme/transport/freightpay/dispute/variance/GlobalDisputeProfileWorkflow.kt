package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global dispute profiles through their life cycle and keeps a journal of what it did. */
data class GlobalDisputeProfileWorkflow(
    var store: GlobalDisputeProfileStore,
    var validator: GlobalDisputeProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDisputeProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global dispute profile $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDisputeProfileStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global dispute profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDisputeProfileStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global dispute profile $code not found")
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

fun newGlobalDisputeProfileWorkflow(store: GlobalDisputeProfileStore, clock: Clock, flags: Registry): GlobalDisputeProfileWorkflow {
    return GlobalDisputeProfileWorkflow(
        store = store,
        validator = defaultGlobalDisputeProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDisputeProfileEngine(flags),
    )
}
