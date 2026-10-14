package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.clock.Clock

/** Moves fallback swap policies through their life cycle and keeps a journal of what it did. */
data class FallbackSwapPolicyCoordinator(
    var store: FallbackSwapPolicyStore,
    var validator: FallbackSwapPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback swap policy $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackSwapPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback swap policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackSwapPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newFallbackSwapPolicyCoordinator(store: FallbackSwapPolicyStore, clock: Clock): FallbackSwapPolicyCoordinator {
    return FallbackSwapPolicyCoordinator(
        store = store,
        validator = defaultFallbackSwapPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
