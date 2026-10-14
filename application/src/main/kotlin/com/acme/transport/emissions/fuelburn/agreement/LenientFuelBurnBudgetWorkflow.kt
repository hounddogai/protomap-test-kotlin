package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient fuel burn budgets through their life cycle and keeps a journal of what it did. */
data class LenientFuelBurnBudgetManager(
    var store: LenientFuelBurnBudgetStore,
    var validator: LenientFuelBurnBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientFuelBurnBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient fuel burn budget $code not found")
        validator.requireValid(item)
        item.moveTo(LenientFuelBurnBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient fuel burn budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientFuelBurnBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient fuel burn budget $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newLenientFuelBurnBudgetManager(store: LenientFuelBurnBudgetStore, clock: Clock, flags: Registry): LenientFuelBurnBudgetManager {
    return LenientFuelBurnBudgetManager(
        store = store,
        validator = defaultLenientFuelBurnBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientFuelBurnBudgetEngine(flags),
    )
}
