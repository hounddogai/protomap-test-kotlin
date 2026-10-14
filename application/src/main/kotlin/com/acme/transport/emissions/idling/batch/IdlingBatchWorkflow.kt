package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling batches through their life cycle and keeps a journal of what it did. */
data class IdlingBatchService(
    var store: IdlingBatchStore,
    var validator: IdlingBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("idling batch $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("idling batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling batch $code not found")
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

fun newIdlingBatchService(store: IdlingBatchStore, clock: Clock, flags: Registry): IdlingBatchService {
    return IdlingBatchService(
        store = store,
        validator = defaultIdlingBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingBatchEngine(flags),
    )
}
