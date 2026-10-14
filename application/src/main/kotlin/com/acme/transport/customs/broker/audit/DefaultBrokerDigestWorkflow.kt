package com.acme.transport.customs.broker.audit

import com.acme.transport.customs.clock.Clock

/** Moves default broker digests through their life cycle and keeps a journal of what it did. */
data class DefaultBrokerDigestWorkflow(
    var store: DefaultBrokerDigestStore,
    var validator: DefaultBrokerDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default broker digest $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultBrokerDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default broker digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultBrokerDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newDefaultBrokerDigestWorkflow(store: DefaultBrokerDigestStore, clock: Clock): DefaultBrokerDigestWorkflow {
    return DefaultBrokerDigestWorkflow(
        store = store,
        validator = defaultDefaultBrokerDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
