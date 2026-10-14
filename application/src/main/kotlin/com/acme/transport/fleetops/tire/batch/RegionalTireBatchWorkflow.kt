package com.acme.transport.fleetops.tire.batch

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional tire batches through their life cycle and keeps a journal of what it did. */
data class RegionalTireBatchWorkflow(
    var store: RegionalTireBatchStore,
    var validator: RegionalTireBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalTireBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional tire batch $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalTireBatchStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional tire batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalTireBatchStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional tire batch $code not found")
        val result = engine.run(item)
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

fun newRegionalTireBatchWorkflow(store: RegionalTireBatchStore, clock: Clock, flags: Registry): RegionalTireBatchWorkflow {
    return RegionalTireBatchWorkflow(
        store = store,
        validator = defaultRegionalTireBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalTireBatchEngine(flags),
    )
}
