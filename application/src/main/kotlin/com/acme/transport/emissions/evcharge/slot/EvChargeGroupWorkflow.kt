package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge groups through their life cycle and keeps a journal of what it did. */
data class EvChargeGroupWorkflow(
    var store: EvChargeGroupStore,
    var validator: EvChargeGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("ev charge group $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeGroupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeGroupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("ev charge group $code not found")
        val result = engine.price(item)
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

fun newEvChargeGroupWorkflow(store: EvChargeGroupStore, clock: Clock, flags: Registry): EvChargeGroupWorkflow {
    return EvChargeGroupWorkflow(
        store = store,
        validator = defaultEvChargeGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeGroupEngine(flags),
    )
}
