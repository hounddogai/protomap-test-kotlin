package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.clock.Clock

/** Moves fallback ev charge claims through their life cycle and keeps a journal of what it did. */
data class FallbackEvChargeClaimManager(
    var store: FallbackEvChargeClaimStore,
    var validator: FallbackEvChargeClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback ev charge claim $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEvChargeClaimStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback ev charge claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEvChargeClaimStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newFallbackEvChargeClaimManager(store: FallbackEvChargeClaimStore, clock: Clock): FallbackEvChargeClaimManager {
    return FallbackEvChargeClaimManager(
        store = store,
        validator = defaultFallbackEvChargeClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
