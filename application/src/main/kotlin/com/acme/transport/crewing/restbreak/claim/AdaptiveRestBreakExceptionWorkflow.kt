package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.clock.Clock

/**
 * Moves adaptive rest break exceptions through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveRestBreakExceptionWorkflow(
    var store: AdaptiveRestBreakExceptionStore,
    var validator: AdaptiveRestBreakExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive rest break exception $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveRestBreakExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive rest break exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveRestBreakExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newAdaptiveRestBreakExceptionWorkflow(store: AdaptiveRestBreakExceptionStore, clock: Clock): AdaptiveRestBreakExceptionWorkflow {
    return AdaptiveRestBreakExceptionWorkflow(
        store = store,
        validator = defaultAdaptiveRestBreakExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
