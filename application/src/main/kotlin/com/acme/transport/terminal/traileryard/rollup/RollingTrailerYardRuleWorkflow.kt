package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.terminal.clock.Clock

/** Moves rolling trailer yard rules through their life cycle and keeps a journal of what it did. */
data class RollingTrailerYardRuleDesk(
    var store: RollingTrailerYardRuleStore,
    var validator: RollingTrailerYardRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling trailer yard rule $code not found")
        validator.requireValid(item)
        item.moveTo(RollingTrailerYardRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling trailer yard rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingTrailerYardRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRollingTrailerYardRuleDesk(store: RollingTrailerYardRuleStore, clock: Clock): RollingTrailerYardRuleDesk {
    return RollingTrailerYardRuleDesk(
        store = store,
        validator = defaultRollingTrailerYardRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
