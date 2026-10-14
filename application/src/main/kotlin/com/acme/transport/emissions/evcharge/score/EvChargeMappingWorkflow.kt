package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge mappings through their life cycle and keeps a journal of what it did. */
data class EvChargeMappingCoordinator(
    var store: EvChargeMappingStore,
    var validator: EvChargeMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("ev charge mapping $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeMappingStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeMappingStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("ev charge mapping $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newEvChargeMappingCoordinator(store: EvChargeMappingStore, clock: Clock, flags: Registry): EvChargeMappingCoordinator {
    return EvChargeMappingCoordinator(
        store = store,
        validator = defaultEvChargeMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeMappingEngine(flags),
    )
}
