package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves service plan batches through their life cycle and keeps a journal of what it did. */
data class ServicePlanBatchWorkflow(
    var store: ServicePlanBatchStore,
    var validator: ServicePlanBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service plan batch $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("service plan batch $code not found")
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

fun newServicePlanBatchWorkflow(store: ServicePlanBatchStore, clock: Clock, flags: Registry): ServicePlanBatchWorkflow {
    return ServicePlanBatchWorkflow(
        store = store,
        validator = defaultServicePlanBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanBatchEngine(flags),
    )
}
