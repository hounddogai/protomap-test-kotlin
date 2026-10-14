package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery mappings through their life cycle and keeps a journal of what it did. */
data class RedeliveryMappingWorkflow(
    var store: RedeliveryMappingStore,
    var validator: RedeliveryMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("redelivery mapping $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("redelivery mapping $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRedeliveryMappingWorkflow(store: RedeliveryMappingStore, clock: Clock): RedeliveryMappingWorkflow {
    return RedeliveryMappingWorkflow(
        store = store,
        validator = defaultRedeliveryMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryMappingEngine(),
    )
}
