package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.crewing.clock.Clock

/** Moves global swap segments through their life cycle and keeps a journal of what it did. */
data class GlobalSwapSegmentManager(
    var store: GlobalSwapSegmentStore,
    var validator: GlobalSwapSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global swap segment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalSwapSegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global swap segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalSwapSegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newGlobalSwapSegmentManager(store: GlobalSwapSegmentStore, clock: Clock): GlobalSwapSegmentManager {
    return GlobalSwapSegmentManager(
        store = store,
        validator = defaultGlobalSwapSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
