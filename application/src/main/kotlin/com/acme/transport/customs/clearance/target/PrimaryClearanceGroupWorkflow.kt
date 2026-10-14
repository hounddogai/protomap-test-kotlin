package com.acme.transport.customs.clearance.target

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary clearance groups through their life cycle and keeps a journal of what it did. */
data class PrimaryClearanceGroupWorkflow(
    var store: PrimaryClearanceGroupStore,
    var validator: PrimaryClearanceGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryClearanceGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary clearance group $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryClearanceGroupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary clearance group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryClearanceGroupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary clearance group $code not found")
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

fun newPrimaryClearanceGroupWorkflow(store: PrimaryClearanceGroupStore, clock: Clock, flags: Registry): PrimaryClearanceGroupWorkflow {
    return PrimaryClearanceGroupWorkflow(
        store = store,
        validator = defaultPrimaryClearanceGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryClearanceGroupEngine(flags),
    )
}
