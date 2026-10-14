package com.acme.transport.lastmile.attempt.template

import com.acme.transport.lastmile.clock.Clock

/** Moves manual attempt quotas through their life cycle and keeps a journal of what it did. */
data class ManualAttemptQuotaManager(
    var store: ManualAttemptQuotaStore,
    var validator: ManualAttemptQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualAttemptQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual attempt quota $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAttemptQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual attempt quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAttemptQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual attempt quota $code not found")
        val result = engine.price(item)
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

fun newManualAttemptQuotaManager(store: ManualAttemptQuotaStore, clock: Clock): ManualAttemptQuotaManager {
    return ManualAttemptQuotaManager(
        store = store,
        validator = defaultManualAttemptQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualAttemptQuotaEngine(),
    )
}
