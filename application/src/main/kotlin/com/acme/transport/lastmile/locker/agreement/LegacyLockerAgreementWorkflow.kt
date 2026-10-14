package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.lastmile.clock.Clock

/** Moves legacy locker agreements through their life cycle and keeps a journal of what it did. */
data class LegacyLockerAgreementWorkflow(
    var store: LegacyLockerAgreementStore,
    var validator: LegacyLockerAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy locker agreement $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyLockerAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy locker agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyLockerAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLegacyLockerAgreementWorkflow(store: LegacyLockerAgreementStore, clock: Clock): LegacyLockerAgreementWorkflow {
    return LegacyLockerAgreementWorkflow(
        store = store,
        validator = defaultLegacyLockerAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
