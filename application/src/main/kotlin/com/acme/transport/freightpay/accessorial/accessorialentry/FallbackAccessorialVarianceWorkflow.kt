package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves fallback accessorial variances through their life cycle and keeps a journal of what it did.
 */
data class FallbackAccessorialVarianceService(
    var store: FallbackAccessorialVarianceStore,
    var validator: FallbackAccessorialVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback accessorial variance $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackAccessorialVarianceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback accessorial variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackAccessorialVarianceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newFallbackAccessorialVarianceService(store: FallbackAccessorialVarianceStore, clock: Clock): FallbackAccessorialVarianceService {
    return FallbackAccessorialVarianceService(
        store = store,
        validator = defaultFallbackAccessorialVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
