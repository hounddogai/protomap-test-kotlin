package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.clock.Clock

/** Moves rest break rules through their life cycle and keeps a journal of what it did. */
data class RestBreakRuleService(
    var store: RestBreakRuleStore,
    var validator: RestBreakRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rest break rule $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRestBreakRuleService(store: RestBreakRuleStore, clock: Clock): RestBreakRuleService {
    return RestBreakRuleService(
        store = store,
        validator = defaultRestBreakRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
