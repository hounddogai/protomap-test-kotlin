package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.terminal.clock.Clock

/** Moves tiered cross dock rules through their life cycle and keeps a journal of what it did. */
data class TieredCrossDockRuleService(
    var store: TieredCrossDockRuleStore,
    var validator: TieredCrossDockRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredCrossDockRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered cross dock rule $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCrossDockRuleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cross dock rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCrossDockRuleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tiered cross dock rule $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newTieredCrossDockRuleService(store: TieredCrossDockRuleStore, clock: Clock): TieredCrossDockRuleService {
    return TieredCrossDockRuleService(
        store = store,
        validator = defaultTieredCrossDockRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredCrossDockRuleEngine(),
    )
}
