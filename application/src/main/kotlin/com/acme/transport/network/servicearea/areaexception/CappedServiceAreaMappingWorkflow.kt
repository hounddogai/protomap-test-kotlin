package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.network.clock.Clock

/** Moves capped service area mappings through their life cycle and keeps a journal of what it did. */
data class CappedServiceAreaMappingWorkflow(
    var store: CappedServiceAreaMappingStore,
    var validator: CappedServiceAreaMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedServiceAreaMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped service area mapping $code not found")
        validator.requireValid(item)
        item.moveTo(CappedServiceAreaMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped service area mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedServiceAreaMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped service area mapping $code not found")
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

fun newCappedServiceAreaMappingWorkflow(store: CappedServiceAreaMappingStore, clock: Clock): CappedServiceAreaMappingWorkflow {
    return CappedServiceAreaMappingWorkflow(
        store = store,
        validator = defaultCappedServiceAreaMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedServiceAreaMappingEngine(),
    )
}
