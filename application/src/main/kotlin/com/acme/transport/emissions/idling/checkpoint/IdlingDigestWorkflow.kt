package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling digests through their life cycle and keeps a journal of what it did. */
data class IdlingDigestCoordinator(
    var store: IdlingDigestStore,
    var validator: IdlingDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("idling digest $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingDigestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("idling digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingDigestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling digest $code not found")
        val result = engine.compute(item)
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

fun newIdlingDigestCoordinator(store: IdlingDigestStore, clock: Clock, flags: Registry): IdlingDigestCoordinator {
    return IdlingDigestCoordinator(
        store = store,
        validator = defaultIdlingDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingDigestEngine(flags),
    )
}
