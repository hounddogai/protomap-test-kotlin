package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.clock.Clock

/** Moves default rest break windows through their life cycle and keeps a journal of what it did. */
data class DefaultRestBreakWindowCoordinator(
    var store: DefaultRestBreakWindowStore,
    var validator: DefaultRestBreakWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default rest break window $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRestBreakWindowStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default rest break window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRestBreakWindowStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newDefaultRestBreakWindowCoordinator(store: DefaultRestBreakWindowStore, clock: Clock): DefaultRestBreakWindowCoordinator {
    return DefaultRestBreakWindowCoordinator(
        store = store,
        validator = defaultDefaultRestBreakWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
