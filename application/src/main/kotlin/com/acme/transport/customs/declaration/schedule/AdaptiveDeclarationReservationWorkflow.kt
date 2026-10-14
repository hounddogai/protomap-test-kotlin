package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves adaptive declaration reservations through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveDeclarationReservationManager(
    var store: AdaptiveDeclarationReservationStore,
    var validator: AdaptiveDeclarationReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDeclarationReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive declaration reservation $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDeclarationReservationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive declaration reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDeclarationReservationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive declaration reservation $code not found")
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

fun newAdaptiveDeclarationReservationManager(store: AdaptiveDeclarationReservationStore, clock: Clock, flags: Registry): AdaptiveDeclarationReservationManager {
    return AdaptiveDeclarationReservationManager(
        store = store,
        validator = defaultAdaptiveDeclarationReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDeclarationReservationEngine(flags),
    )
}
