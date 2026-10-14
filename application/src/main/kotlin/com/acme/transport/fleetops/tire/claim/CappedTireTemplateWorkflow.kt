package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped tire templates through their life cycle and keeps a journal of what it did. */
data class CappedTireTemplateDesk(
    var store: CappedTireTemplateStore,
    var validator: CappedTireTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTireTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped tire template $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTireTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped tire template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTireTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped tire template $code not found")
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

fun newCappedTireTemplateDesk(store: CappedTireTemplateStore, clock: Clock, flags: Registry): CappedTireTemplateDesk {
    return CappedTireTemplateDesk(
        store = store,
        validator = defaultCappedTireTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTireTemplateEngine(flags),
    )
}
