package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves global rate card assignments through their life cycle and keeps a journal of what it did. */
data class GlobalRateCardAssignmentService(
    var store: GlobalRateCardAssignmentStore,
    var validator: GlobalRateCardAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRateCardAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global rate card assignment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRateCardAssignmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global rate card assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRateCardAssignmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global rate card assignment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newGlobalRateCardAssignmentService(store: GlobalRateCardAssignmentStore, clock: Clock): GlobalRateCardAssignmentService {
    return GlobalRateCardAssignmentService(
        store = store,
        validator = defaultGlobalRateCardAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRateCardAssignmentEngine(),
    )
}
