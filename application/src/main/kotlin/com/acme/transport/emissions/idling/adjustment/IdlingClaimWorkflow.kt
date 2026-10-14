package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling claims through their life cycle and keeps a journal of what it did. */
data class IdlingClaimDesk(
    var store: IdlingClaimStore,
    var validator: IdlingClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("idling claim $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("idling claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("idling claim $code not found")
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

fun newIdlingClaimDesk(store: IdlingClaimStore, clock: Clock, flags: Registry): IdlingClaimDesk {
    return IdlingClaimDesk(
        store = store,
        validator = defaultIdlingClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingClaimEngine(flags),
    )
}
