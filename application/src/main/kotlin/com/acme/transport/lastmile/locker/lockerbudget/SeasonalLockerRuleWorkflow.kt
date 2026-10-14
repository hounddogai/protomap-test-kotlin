package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal locker rules through their life cycle and keeps a journal of what it did. */
data class SeasonalLockerRuleManager(
    var store: SeasonalLockerRuleStore,
    var validator: SeasonalLockerRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalLockerRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal locker rule $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalLockerRuleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal locker rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalLockerRuleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("seasonal locker rule $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newSeasonalLockerRuleManager(store: SeasonalLockerRuleStore, clock: Clock): SeasonalLockerRuleManager {
    return SeasonalLockerRuleManager(
        store = store,
        validator = defaultSeasonalLockerRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalLockerRuleEngine(),
    )
}
