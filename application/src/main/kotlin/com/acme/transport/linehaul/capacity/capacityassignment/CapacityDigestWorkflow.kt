package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity digests through their life cycle and keeps a journal of what it did. */
data class CapacityDigestWorkflow(
    var store: CapacityDigestStore,
    var validator: CapacityDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capacity digest $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capacity digest $code not found")
        val result = engine.evaluate(item)
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

fun newCapacityDigestWorkflow(store: CapacityDigestStore, clock: Clock): CapacityDigestWorkflow {
    return CapacityDigestWorkflow(
        store = store,
        validator = defaultCapacityDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityDigestEngine(),
    )
}
