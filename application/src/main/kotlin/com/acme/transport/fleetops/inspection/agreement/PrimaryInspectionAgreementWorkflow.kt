package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.clock.Clock

/** Moves primary inspection agreements through their life cycle and keeps a journal of what it did. */
data class PrimaryInspectionAgreementWorkflow(
    var store: PrimaryInspectionAgreementStore,
    var validator: PrimaryInspectionAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary inspection agreement $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryInspectionAgreementStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary inspection agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryInspectionAgreementStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newPrimaryInspectionAgreementWorkflow(store: PrimaryInspectionAgreementStore, clock: Clock): PrimaryInspectionAgreementWorkflow {
    return PrimaryInspectionAgreementWorkflow(
        store = store,
        validator = defaultPrimaryInspectionAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
