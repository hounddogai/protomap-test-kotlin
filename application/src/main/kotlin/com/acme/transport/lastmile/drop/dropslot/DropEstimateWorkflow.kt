package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves drop estimates through their life cycle and keeps a journal of what it did. */
data class DropEstimateWorkflow(
    var store: DropEstimateStore,
    var validator: DropEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("drop estimate $code not found")
        validator.requireValid(item)
        item.moveTo(DropEstimateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("drop estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropEstimateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("drop estimate $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDropEstimateWorkflow(store: DropEstimateStore, clock: Clock, flags: Registry): DropEstimateWorkflow {
    return DropEstimateWorkflow(
        store = store,
        validator = defaultDropEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropEstimateEngine(flags),
    )
}
