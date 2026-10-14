package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.clock.Clock

/** Moves tire agreements through their life cycle and keeps a journal of what it did. */
data class TireAgreementService(
    var store: TireAgreementStore,
    var validator: TireAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tire agreement $code not found")
        validator.requireValid(item)
        item.moveTo(TireAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tire agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTireAgreementService(store: TireAgreementStore, clock: Clock): TireAgreementService {
    return TireAgreementService(
        store = store,
        validator = defaultTireAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
