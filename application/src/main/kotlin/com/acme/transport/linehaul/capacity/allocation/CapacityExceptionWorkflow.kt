package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capacity exceptions through their life cycle and keeps a journal of what it did. */
data class CapacityExceptionWorkflow(
    var store: CapacityExceptionStore,
    var validator: CapacityExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capacity exception $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityExceptionStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityExceptionStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capacity exception $code not found")
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

fun newCapacityExceptionWorkflow(store: CapacityExceptionStore, clock: Clock, flags: Registry): CapacityExceptionWorkflow {
    return CapacityExceptionWorkflow(
        store = store,
        validator = defaultCapacityExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityExceptionEngine(flags),
    )
}
