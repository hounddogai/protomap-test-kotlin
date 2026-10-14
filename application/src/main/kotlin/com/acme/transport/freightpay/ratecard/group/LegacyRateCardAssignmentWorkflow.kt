package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy rate card assignments through their life cycle and keeps a journal of what it did. */
data class LegacyRateCardAssignmentManager(
    var store: LegacyRateCardAssignmentStore,
    var validator: LegacyRateCardAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy rate card assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyRateCardAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy rate card assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyRateCardAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLegacyRateCardAssignmentManager(store: LegacyRateCardAssignmentStore, clock: Clock): LegacyRateCardAssignmentManager {
    return LegacyRateCardAssignmentManager(
        store = store,
        validator = defaultLegacyRateCardAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
