package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default accessorial cycles through their life cycle and keeps a journal of what it did. */
data class DefaultAccessorialCycleWorkflow(
    var store: DefaultAccessorialCycleStore,
    var validator: DefaultAccessorialCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultAccessorialCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default accessorial cycle $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAccessorialCycleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default accessorial cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAccessorialCycleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default accessorial cycle $code not found")
        val result = engine.compute(item)
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

fun newDefaultAccessorialCycleWorkflow(store: DefaultAccessorialCycleStore, clock: Clock, flags: Registry): DefaultAccessorialCycleWorkflow {
    return DefaultAccessorialCycleWorkflow(
        store = store,
        validator = defaultDefaultAccessorialCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultAccessorialCycleEngine(flags),
    )
}
