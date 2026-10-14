package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break tiers through their life cycle and keeps a journal of what it did. */
data class RestBreakTierDesk(
    var store: RestBreakTierStore,
    var validator: RestBreakTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rest break tier $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakTierStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakTierStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rest break tier $code not found")
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

fun newRestBreakTierDesk(store: RestBreakTierStore, clock: Clock, flags: Registry): RestBreakTierDesk {
    return RestBreakTierDesk(
        store = store,
        validator = defaultRestBreakTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakTierEngine(flags),
    )
}
