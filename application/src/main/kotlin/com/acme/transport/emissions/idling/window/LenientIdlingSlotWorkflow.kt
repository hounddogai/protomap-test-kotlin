package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient idling slots through their life cycle and keeps a journal of what it did. */
data class LenientIdlingSlotCoordinator(
    var store: LenientIdlingSlotStore,
    var validator: LenientIdlingSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientIdlingSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient idling slot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientIdlingSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient idling slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientIdlingSlotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient idling slot $code not found")
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

fun newLenientIdlingSlotCoordinator(store: LenientIdlingSlotStore, clock: Clock, flags: Registry): LenientIdlingSlotCoordinator {
    return LenientIdlingSlotCoordinator(
        store = store,
        validator = defaultLenientIdlingSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientIdlingSlotEngine(flags),
    )
}
