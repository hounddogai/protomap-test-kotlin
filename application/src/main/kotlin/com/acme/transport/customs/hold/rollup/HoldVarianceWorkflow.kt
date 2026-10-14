package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.clock.Clock

/** Moves hold variances through their life cycle and keeps a journal of what it did. */
data class HoldVarianceDesk(
    var store: HoldVarianceStore,
    var validator: HoldVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hold variance $code not found")
        validator.requireValid(item)
        item.moveTo(HoldVarianceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("hold variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldVarianceStatus.ARCHIVED)
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

fun newHoldVarianceDesk(store: HoldVarianceStore, clock: Clock): HoldVarianceDesk {
    return HoldVarianceDesk(
        store = store,
        validator = defaultHoldVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
