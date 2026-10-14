package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.clock.Clock

/** Moves rest break profiles through their life cycle and keeps a journal of what it did. */
data class RestBreakProfileManager(
    var store: RestBreakProfileStore,
    var validator: RestBreakProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rest break profile $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakProfileStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakProfileStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rest break profile $code not found")
        val result = engine.run(item)
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

fun newRestBreakProfileManager(store: RestBreakProfileStore, clock: Clock): RestBreakProfileManager {
    return RestBreakProfileManager(
        store = store,
        validator = defaultRestBreakProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakProfileEngine(),
    )
}
