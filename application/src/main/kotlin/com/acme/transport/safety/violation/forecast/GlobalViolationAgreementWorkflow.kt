package com.acme.transport.safety.violation.forecast

import com.acme.transport.safety.clock.Clock

/** Moves global violation agreements through their life cycle and keeps a journal of what it did. */
data class GlobalViolationAgreementDesk(
    var store: GlobalViolationAgreementStore,
    var validator: GlobalViolationAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global violation agreement $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalViolationAgreementStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global violation agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalViolationAgreementStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalViolationAgreementDesk(store: GlobalViolationAgreementStore, clock: Clock): GlobalViolationAgreementDesk {
    return GlobalViolationAgreementDesk(
        store = store,
        validator = defaultGlobalViolationAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
