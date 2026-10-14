package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.network.clock.Clock

/** Moves adaptive service area claims through their life cycle and keeps a journal of what it did. */
data class AdaptiveServiceAreaClaimService(
    var store: AdaptiveServiceAreaClaimStore,
    var validator: AdaptiveServiceAreaClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive service area claim $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveServiceAreaClaimStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive service area claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveServiceAreaClaimStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newAdaptiveServiceAreaClaimService(store: AdaptiveServiceAreaClaimStore, clock: Clock): AdaptiveServiceAreaClaimService {
    return AdaptiveServiceAreaClaimService(
        store = store,
        validator = defaultAdaptiveServiceAreaClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
