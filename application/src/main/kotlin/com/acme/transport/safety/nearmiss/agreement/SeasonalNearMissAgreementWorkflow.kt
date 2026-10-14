package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.safety.clock.Clock

/** Moves seasonal near miss agreements through their life cycle and keeps a journal of what it did. */
data class SeasonalNearMissAgreementService(
    var store: SeasonalNearMissAgreementStore,
    var validator: SeasonalNearMissAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal near miss agreement $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalNearMissAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal near miss agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalNearMissAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newSeasonalNearMissAgreementService(store: SeasonalNearMissAgreementStore, clock: Clock): SeasonalNearMissAgreementService {
    return SeasonalNearMissAgreementService(
        store = store,
        validator = defaultSeasonalNearMissAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
