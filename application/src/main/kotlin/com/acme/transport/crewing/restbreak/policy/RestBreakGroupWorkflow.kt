package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.clock.Clock

/** Moves rest break groups through their life cycle and keeps a journal of what it did. */
data class RestBreakGroupDesk(
    var store: RestBreakGroupStore,
    var validator: RestBreakGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rest break group $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRestBreakGroupDesk(store: RestBreakGroupStore, clock: Clock): RestBreakGroupDesk {
    return RestBreakGroupDesk(
        store = store,
        validator = defaultRestBreakGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
