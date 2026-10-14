package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.safety.clock.Clock

/** Moves strict near miss policies through their life cycle and keeps a journal of what it did. */
data class StrictNearMissPolicyDesk(
    var store: StrictNearMissPolicyStore,
    var validator: StrictNearMissPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict near miss policy $code not found")
        validator.requireValid(item)
        item.moveTo(StrictNearMissPolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict near miss policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictNearMissPolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newStrictNearMissPolicyDesk(store: StrictNearMissPolicyStore, clock: Clock): StrictNearMissPolicyDesk {
    return StrictNearMissPolicyDesk(
        store = store,
        validator = defaultStrictNearMissPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
