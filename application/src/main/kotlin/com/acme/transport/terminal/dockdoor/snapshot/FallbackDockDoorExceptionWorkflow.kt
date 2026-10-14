package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.terminal.clock.Clock

/** Moves fallback dock door exceptions through their life cycle and keeps a journal of what it did. */
data class FallbackDockDoorExceptionDesk(
    var store: FallbackDockDoorExceptionStore,
    var validator: FallbackDockDoorExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback dock door exception $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDockDoorExceptionStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback dock door exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDockDoorExceptionStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newFallbackDockDoorExceptionDesk(store: FallbackDockDoorExceptionStore, clock: Clock): FallbackDockDoorExceptionDesk {
    return FallbackDockDoorExceptionDesk(
        store = store,
        validator = defaultFallbackDockDoorExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
