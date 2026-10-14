package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.clock.Clock

/** Moves fallback idling variances through their life cycle and keeps a journal of what it did. */
data class FallbackIdlingVarianceDesk(
    var store: FallbackIdlingVarianceStore,
    var validator: FallbackIdlingVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback idling variance $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackIdlingVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback idling variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackIdlingVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFallbackIdlingVarianceDesk(store: FallbackIdlingVarianceStore, clock: Clock): FallbackIdlingVarianceDesk {
    return FallbackIdlingVarianceDesk(
        store = store,
        validator = defaultFallbackIdlingVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
