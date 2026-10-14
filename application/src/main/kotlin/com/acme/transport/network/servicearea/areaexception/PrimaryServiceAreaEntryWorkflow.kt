package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves primary service area entries through their life cycle and keeps a journal of what it did. */
data class PrimaryServiceAreaEntryWorkflow(
    var store: PrimaryServiceAreaEntryStore,
    var validator: PrimaryServiceAreaEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryServiceAreaEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary service area entry $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryServiceAreaEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary service area entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryServiceAreaEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary service area entry $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newPrimaryServiceAreaEntryWorkflow(store: PrimaryServiceAreaEntryStore, clock: Clock, flags: Registry): PrimaryServiceAreaEntryWorkflow {
    return PrimaryServiceAreaEntryWorkflow(
        store = store,
        validator = defaultPrimaryServiceAreaEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryServiceAreaEntryEngine(flags),
    )
}
