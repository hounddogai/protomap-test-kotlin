package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.linehaul.clock.Clock

/** Moves strict backhaul mappings through their life cycle and keeps a journal of what it did. */
data class StrictBackhaulMappingCoordinator(
    var store: StrictBackhaulMappingStore,
    var validator: StrictBackhaulMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictBackhaulMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict backhaul mapping $code not found")
        validator.requireValid(item)
        item.moveTo(StrictBackhaulMappingStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict backhaul mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictBackhaulMappingStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict backhaul mapping $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newStrictBackhaulMappingCoordinator(store: StrictBackhaulMappingStore, clock: Clock): StrictBackhaulMappingCoordinator {
    return StrictBackhaulMappingCoordinator(
        store = store,
        validator = defaultStrictBackhaulMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictBackhaulMappingEngine(),
    )
}
