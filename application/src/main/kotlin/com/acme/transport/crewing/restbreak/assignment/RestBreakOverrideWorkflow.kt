package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.clock.Clock

/** Moves rest break overrides through their life cycle and keeps a journal of what it did. */
data class RestBreakOverrideService(
    var store: RestBreakOverrideStore,
    var validator: RestBreakOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rest break override $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rest break override $code not found")
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

fun newRestBreakOverrideService(store: RestBreakOverrideStore, clock: Clock): RestBreakOverrideService {
    return RestBreakOverrideService(
        store = store,
        validator = defaultRestBreakOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakOverrideEngine(),
    )
}
