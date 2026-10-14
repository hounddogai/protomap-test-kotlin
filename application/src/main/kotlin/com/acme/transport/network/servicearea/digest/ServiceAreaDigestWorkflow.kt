package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves service area digests through their life cycle and keeps a journal of what it did. */
data class ServiceAreaDigestService(
    var store: ServiceAreaDigestStore,
    var validator: ServiceAreaDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServiceAreaDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("service area digest $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service area digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaDigestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("service area digest $code not found")
        val result = engine.evaluate(item)
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

fun newServiceAreaDigestService(store: ServiceAreaDigestStore, clock: Clock, flags: Registry): ServiceAreaDigestService {
    return ServiceAreaDigestService(
        store = store,
        validator = defaultServiceAreaDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServiceAreaDigestEngine(flags),
    )
}
