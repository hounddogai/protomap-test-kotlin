package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves drop rules through their life cycle and keeps a journal of what it did. */
data class DropRuleDesk(
    var store: DropRuleStore,
    var validator: DropRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("drop rule $code not found")
        validator.requireValid(item)
        item.moveTo(DropRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("drop rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("drop rule $code not found")
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

fun newDropRuleDesk(store: DropRuleStore, clock: Clock, flags: Registry): DropRuleDesk {
    return DropRuleDesk(
        store = store,
        validator = defaultDropRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropRuleEngine(flags),
    )
}
