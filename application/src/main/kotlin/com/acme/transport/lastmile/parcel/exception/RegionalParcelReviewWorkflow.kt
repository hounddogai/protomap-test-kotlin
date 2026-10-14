package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.lastmile.clock.Clock

/** Moves regional parcel reviews through their life cycle and keeps a journal of what it did. */
data class RegionalParcelReviewWorkflow(
    var store: RegionalParcelReviewStore,
    var validator: RegionalParcelReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional parcel review $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalParcelReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional parcel review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalParcelReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRegionalParcelReviewWorkflow(store: RegionalParcelReviewStore, clock: Clock): RegionalParcelReviewWorkflow {
    return RegionalParcelReviewWorkflow(
        store = store,
        validator = defaultRegionalParcelReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
