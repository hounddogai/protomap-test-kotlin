package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute preferences through their life cycle and keeps a journal of what it did. */
data class DisputePreferenceManager(
    var store: DisputePreferenceStore,
    var validator: DisputePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputePreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dispute preference $code not found")
        validator.requireValid(item)
        item.moveTo(DisputePreferenceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputePreferenceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("dispute preference $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newDisputePreferenceManager(store: DisputePreferenceStore, clock: Clock): DisputePreferenceManager {
    return DisputePreferenceManager(
        store = store,
        validator = defaultDisputePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputePreferenceEngine(),
    )
}
