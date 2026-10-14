package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive trailer requests through their life cycle and keeps a journal of what it did. */
data class AdaptiveTrailerRequestWorkflow(
    var store: AdaptiveTrailerRequestStore,
    var validator: AdaptiveTrailerRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive trailer request $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTrailerRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive trailer request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTrailerRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newAdaptiveTrailerRequestWorkflow(store: AdaptiveTrailerRequestStore, clock: Clock): AdaptiveTrailerRequestWorkflow {
    return AdaptiveTrailerRequestWorkflow(
        store = store,
        validator = defaultAdaptiveTrailerRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
