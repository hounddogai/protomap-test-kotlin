package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves locker rules through their life cycle and keeps a journal of what it did. */
data class LockerRuleManager(
    var store: LockerRuleStore,
    var validator: LockerRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("locker rule $code not found")
        validator.requireValid(item)
        item.moveTo(LockerRuleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("locker rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerRuleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("locker rule $code not found")
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

fun newLockerRuleManager(store: LockerRuleStore, clock: Clock, flags: Registry): LockerRuleManager {
    return LockerRuleManager(
        store = store,
        validator = defaultLockerRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerRuleEngine(flags),
    )
}
