package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched broker rules through their life cycle and keeps a journal of what it did. */
data class BatchedBrokerRuleManager(
    var store: BatchedBrokerRuleStore,
    var validator: BatchedBrokerRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedBrokerRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched broker rule $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedBrokerRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched broker rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedBrokerRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched broker rule $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newBatchedBrokerRuleManager(store: BatchedBrokerRuleStore, clock: Clock, flags: Registry): BatchedBrokerRuleManager {
    return BatchedBrokerRuleManager(
        store = store,
        validator = defaultBatchedBrokerRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedBrokerRuleEngine(flags),
    )
}
