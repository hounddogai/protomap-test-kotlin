package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute agreements through their life cycle and keeps a journal of what it did. */
data class DisputeAgreementWorkflow(
    var store: DisputeAgreementStore,
    var validator: DisputeAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dispute agreement $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeAgreementStatus.RETIRED)
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

fun newDisputeAgreementWorkflow(store: DisputeAgreementStore, clock: Clock): DisputeAgreementWorkflow {
    return DisputeAgreementWorkflow(
        store = store,
        validator = defaultDisputeAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
