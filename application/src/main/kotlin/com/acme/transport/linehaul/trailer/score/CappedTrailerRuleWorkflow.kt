package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capped trailer rules through their life cycle and keeps a journal of what it did. */
data class CappedTrailerRuleManager(
    var store: CappedTrailerRuleStore,
    var validator: CappedTrailerRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTrailerRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped trailer rule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped trailer rule $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newCappedTrailerRuleManager(store: CappedTrailerRuleStore, clock: Clock, flags: Registry): CappedTrailerRuleManager {
    return CappedTrailerRuleManager(
        store = store,
        validator = defaultCappedTrailerRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTrailerRuleEngine(flags),
    )
}
