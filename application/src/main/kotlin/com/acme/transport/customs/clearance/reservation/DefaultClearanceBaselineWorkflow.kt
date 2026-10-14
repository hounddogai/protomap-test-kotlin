package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.clock.Clock

/** Moves default clearance baselines through their life cycle and keeps a journal of what it did. */
data class DefaultClearanceBaselineService(
    var store: DefaultClearanceBaselineStore,
    var validator: DefaultClearanceBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default clearance baseline $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultClearanceBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default clearance baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultClearanceBaselineStatus.ARCHIVED)
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

fun newDefaultClearanceBaselineService(store: DefaultClearanceBaselineStore, clock: Clock): DefaultClearanceBaselineService {
    return DefaultClearanceBaselineService(
        store = store,
        validator = defaultDefaultClearanceBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
