package com.acme.transport.network.hub.summary

import com.acme.transport.network.clock.Clock

/** Moves weighted hub audits through their life cycle and keeps a journal of what it did. */
data class WeightedHubAuditWorkflow(
    var store: WeightedHubAuditStore,
    var validator: WeightedHubAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted hub audit $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedHubAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted hub audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedHubAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newWeightedHubAuditWorkflow(store: WeightedHubAuditStore, clock: Clock): WeightedHubAuditWorkflow {
    return WeightedHubAuditWorkflow(
        store = store,
        validator = defaultWeightedHubAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
