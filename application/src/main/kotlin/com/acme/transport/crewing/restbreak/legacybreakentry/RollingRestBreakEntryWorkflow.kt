package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling rest break entries through their life cycle and keeps a journal of what it did. */
data class RollingRestBreakEntryManager(
    var store: RollingRestBreakEntryStore,
    var validator: RollingRestBreakEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingRestBreakEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling rest break entry $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRestBreakEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling rest break entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRestBreakEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling rest break entry $code not found")
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

fun newRollingRestBreakEntryManager(store: RollingRestBreakEntryStore, clock: Clock, flags: Registry): RollingRestBreakEntryManager {
    return RollingRestBreakEntryManager(
        store = store,
        validator = defaultRollingRestBreakEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingRestBreakEntryEngine(flags),
    )
}
