package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fuel burn templates through their life cycle and keeps a journal of what it did. */
data class FuelBurnTemplateDesk(
    var store: FuelBurnTemplateStore,
    var validator: FuelBurnTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelBurnTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel burn template $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnTemplateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnTemplateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fuel burn template $code not found")
        val result = engine.run(item)
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

fun newFuelBurnTemplateDesk(store: FuelBurnTemplateStore, clock: Clock, flags: Registry): FuelBurnTemplateDesk {
    return FuelBurnTemplateDesk(
        store = store,
        validator = defaultFuelBurnTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelBurnTemplateEngine(flags),
    )
}
