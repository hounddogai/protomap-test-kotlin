package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.fleetops.clock.Clock

/** Moves capped service plan entries through their life cycle and keeps a journal of what it did. */
data class CappedServicePlanEntryService(
    var store: CappedServicePlanEntryStore,
    var validator: CappedServicePlanEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedServicePlanEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped service plan entry $code not found")
        validator.requireValid(item)
        item.moveTo(CappedServicePlanEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped service plan entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedServicePlanEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped service plan entry $code not found")
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

fun newCappedServicePlanEntryService(store: CappedServicePlanEntryStore, clock: Clock): CappedServicePlanEntryService {
    return CappedServicePlanEntryService(
        store = store,
        validator = defaultCappedServicePlanEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedServicePlanEntryEngine(),
    )
}
