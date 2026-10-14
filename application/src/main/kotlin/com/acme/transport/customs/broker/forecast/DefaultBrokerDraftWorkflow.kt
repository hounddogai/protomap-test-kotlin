package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.clock.Clock

/** Moves default broker drafts through their life cycle and keeps a journal of what it did. */
data class DefaultBrokerDraftCoordinator(
    var store: DefaultBrokerDraftStore,
    var validator: DefaultBrokerDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default broker draft $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultBrokerDraftStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default broker draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultBrokerDraftStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDefaultBrokerDraftCoordinator(store: DefaultBrokerDraftStore, clock: Clock): DefaultBrokerDraftCoordinator {
    return DefaultBrokerDraftCoordinator(
        store = store,
        validator = defaultDefaultBrokerDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
