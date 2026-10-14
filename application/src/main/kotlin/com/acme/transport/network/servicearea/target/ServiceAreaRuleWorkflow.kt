package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves service area rules through their life cycle and keeps a journal of what it did. */
data class ServiceAreaRuleService(
    var store: ServiceAreaRuleStore,
    var validator: ServiceAreaRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServiceAreaRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service area rule $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("service area rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("service area rule $code not found")
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

fun newServiceAreaRuleService(store: ServiceAreaRuleStore, clock: Clock, flags: Registry): ServiceAreaRuleService {
    return ServiceAreaRuleService(
        store = store,
        validator = defaultServiceAreaRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServiceAreaRuleEngine(flags),
    )
}
