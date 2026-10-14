package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves scheduled proof of delivery entries through their life cycle and keeps a journal of what it did.
 */
data class ScheduledProofOfDeliveryEntryWorkflow(
    var store: ScheduledProofOfDeliveryEntryStore,
    var validator: ScheduledProofOfDeliveryEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled proof of delivery entry $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledProofOfDeliveryEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled proof of delivery entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledProofOfDeliveryEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newScheduledProofOfDeliveryEntryWorkflow(store: ScheduledProofOfDeliveryEntryStore, clock: Clock): ScheduledProofOfDeliveryEntryWorkflow {
    return ScheduledProofOfDeliveryEntryWorkflow(
        store = store,
        validator = defaultScheduledProofOfDeliveryEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
