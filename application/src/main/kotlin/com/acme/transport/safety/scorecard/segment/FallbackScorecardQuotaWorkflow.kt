package com.acme.transport.safety.scorecard.segment

import com.acme.transport.safety.clock.Clock

/** Moves fallback scorecard quotas through their life cycle and keeps a journal of what it did. */
data class FallbackScorecardQuotaManager(
    var store: FallbackScorecardQuotaStore,
    var validator: FallbackScorecardQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback scorecard quota $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackScorecardQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback scorecard quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackScorecardQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newFallbackScorecardQuotaManager(store: FallbackScorecardQuotaStore, clock: Clock): FallbackScorecardQuotaManager {
    return FallbackScorecardQuotaManager(
        store = store,
        validator = defaultFallbackScorecardQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
