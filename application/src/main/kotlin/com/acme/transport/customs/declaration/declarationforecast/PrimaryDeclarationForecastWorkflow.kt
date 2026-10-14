package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary declaration forecasts through their life cycle and keeps a journal of what it did. */
data class PrimaryDeclarationForecastCoordinator(
    var store: PrimaryDeclarationForecastStore,
    var validator: PrimaryDeclarationForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDeclarationForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary declaration forecast $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDeclarationForecastStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary declaration forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDeclarationForecastStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary declaration forecast $code not found")
        val result = engine.compute(item)
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

fun newPrimaryDeclarationForecastCoordinator(store: PrimaryDeclarationForecastStore, clock: Clock, flags: Registry): PrimaryDeclarationForecastCoordinator {
    return PrimaryDeclarationForecastCoordinator(
        store = store,
        validator = defaultPrimaryDeclarationForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDeclarationForecastEngine(flags),
    )
}
