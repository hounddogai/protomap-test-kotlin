package com.acme.transport.network.transittime.summary

import com.acme.transport.network.clock.Clock

/** Moves default transit time quotas through their life cycle and keeps a journal of what it did. */
data class DefaultTransitTimeQuotaWorkflow(
    var store: DefaultTransitTimeQuotaStore,
    var validator: DefaultTransitTimeQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default transit time quota $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTransitTimeQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default transit time quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTransitTimeQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDefaultTransitTimeQuotaWorkflow(store: DefaultTransitTimeQuotaStore, clock: Clock): DefaultTransitTimeQuotaWorkflow {
    return DefaultTransitTimeQuotaWorkflow(
        store = store,
        validator = defaultDefaultTransitTimeQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
