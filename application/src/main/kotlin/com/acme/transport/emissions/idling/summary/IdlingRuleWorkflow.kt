package com.acme.transport.emissions.idling.summary

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling rules through their life cycle and keeps a journal of what it did. */
data class IdlingRuleService(
    var store: IdlingRuleStore,
    var validator: IdlingRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("idling rule $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("idling rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling rule $code not found")
        val result = engine.price(item)
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

fun newIdlingRuleService(store: IdlingRuleStore, clock: Clock, flags: Registry): IdlingRuleService {
    return IdlingRuleService(
        store = store,
        validator = defaultIdlingRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingRuleEngine(flags),
    )
}
