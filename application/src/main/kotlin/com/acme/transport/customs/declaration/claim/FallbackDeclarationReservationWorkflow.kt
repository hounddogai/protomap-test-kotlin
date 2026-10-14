package com.acme.transport.customs.declaration.claim

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback declaration reservations through their life cycle and keeps a journal of what it did.
 */
data class FallbackDeclarationReservationDesk(
    var store: FallbackDeclarationReservationStore,
    var validator: FallbackDeclarationReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDeclarationReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback declaration reservation $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDeclarationReservationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback declaration reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDeclarationReservationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback declaration reservation $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newFallbackDeclarationReservationDesk(store: FallbackDeclarationReservationStore, clock: Clock, flags: Registry): FallbackDeclarationReservationDesk {
    return FallbackDeclarationReservationDesk(
        store = store,
        validator = defaultFallbackDeclarationReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDeclarationReservationEngine(flags),
    )
}
