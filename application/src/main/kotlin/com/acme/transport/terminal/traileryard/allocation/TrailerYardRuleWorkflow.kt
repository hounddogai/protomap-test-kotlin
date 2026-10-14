package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard rules through their life cycle and keeps a journal of what it did. */
data class TrailerYardRuleCoordinator(
    var store: TrailerYardRuleStore,
    var validator: TrailerYardRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer yard rule $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTrailerYardRuleCoordinator(store: TrailerYardRuleStore, clock: Clock): TrailerYardRuleCoordinator {
    return TrailerYardRuleCoordinator(
        store = store,
        validator = defaultTrailerYardRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
