package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves service area rollups through their life cycle and keeps a journal of what it did. */
data class ServiceAreaRollupCoordinator(
    var store: ServiceAreaRollupStore,
    var validator: ServiceAreaRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServiceAreaRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service area rollup $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaRollupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("service area rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaRollupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("service area rollup $code not found")
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

fun newServiceAreaRollupCoordinator(store: ServiceAreaRollupStore, clock: Clock, flags: Registry): ServiceAreaRollupCoordinator {
    return ServiceAreaRollupCoordinator(
        store = store,
        validator = defaultServiceAreaRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServiceAreaRollupEngine(flags),
    )
}
