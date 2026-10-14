package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.network.clock.Clock

/** Moves manual hub quotas through their life cycle and keeps a journal of what it did. */
data class ManualHubQuotaService(
    var store: ManualHubQuotaStore,
    var validator: ManualHubQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual hub quota $code not found")
        validator.requireValid(item)
        item.moveTo(ManualHubQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual hub quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualHubQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newManualHubQuotaService(store: ManualHubQuotaStore, clock: Clock): ManualHubQuotaService {
    return ManualHubQuotaService(
        store = store,
        validator = defaultManualHubQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
