package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lenient relay rules through their life cycle and keeps a journal of what it did. */
data class LenientRelayRuleDesk(
    var store: LenientRelayRuleStore,
    var validator: LenientRelayRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientRelayRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient relay rule $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRelayRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient relay rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRelayRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient relay rule $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newLenientRelayRuleDesk(store: LenientRelayRuleStore, clock: Clock, flags: Registry): LenientRelayRuleDesk {
    return LenientRelayRuleDesk(
        store = store,
        validator = defaultLenientRelayRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientRelayRuleEngine(flags),
    )
}
