package com.acme.transport.customs.broker.group

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled broker digests through their life cycle and keeps a journal of what it did. */
data class ScheduledBrokerDigestWorkflow(
    var store: ScheduledBrokerDigestStore,
    var validator: ScheduledBrokerDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledBrokerDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled broker digest $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledBrokerDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled broker digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledBrokerDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled broker digest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledBrokerDigestWorkflow(store: ScheduledBrokerDigestStore, clock: Clock, flags: Registry): ScheduledBrokerDigestWorkflow {
    return ScheduledBrokerDigestWorkflow(
        store = store,
        validator = defaultScheduledBrokerDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledBrokerDigestEngine(flags),
    )
}
