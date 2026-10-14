package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves service area overrides through their life cycle and keeps a journal of what it did. */
data class ServiceAreaOverrideWorkflow(
    var store: ServiceAreaOverrideStore,
    var validator: ServiceAreaOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServiceAreaOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service area override $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service area override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("service area override $code not found")
        val result = engine.price(item)
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

fun newServiceAreaOverrideWorkflow(store: ServiceAreaOverrideStore, clock: Clock, flags: Registry): ServiceAreaOverrideWorkflow {
    return ServiceAreaOverrideWorkflow(
        store = store,
        validator = defaultServiceAreaOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServiceAreaOverrideEngine(flags),
    )
}
