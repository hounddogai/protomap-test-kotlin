package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.clock.Clock

/** Moves swap drafts through their life cycle and keeps a journal of what it did. */
data class SwapDraftCoordinator(
    var store: SwapDraftStore,
    var validator: SwapDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("swap draft $code not found")
        validator.requireValid(item)
        item.moveTo(SwapDraftStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("swap draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapDraftStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("swap draft $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newSwapDraftCoordinator(store: SwapDraftStore, clock: Clock): SwapDraftCoordinator {
    return SwapDraftCoordinator(
        store = store,
        validator = defaultSwapDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapDraftEngine(),
    )
}
