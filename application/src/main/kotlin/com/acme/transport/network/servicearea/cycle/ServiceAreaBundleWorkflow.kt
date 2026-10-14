package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves service area bundles through their life cycle and keeps a journal of what it did. */
data class ServiceAreaBundleService(
    var store: ServiceAreaBundleStore,
    var validator: ServiceAreaBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServiceAreaBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service area bundle $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaBundleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("service area bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaBundleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("service area bundle $code not found")
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

fun newServiceAreaBundleService(store: ServiceAreaBundleStore, clock: Clock, flags: Registry): ServiceAreaBundleService {
    return ServiceAreaBundleService(
        store = store,
        validator = defaultServiceAreaBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServiceAreaBundleEngine(flags),
    )
}
