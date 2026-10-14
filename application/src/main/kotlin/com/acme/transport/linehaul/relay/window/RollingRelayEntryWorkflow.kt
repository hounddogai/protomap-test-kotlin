package com.acme.transport.linehaul.relay.window

import com.acme.transport.linehaul.clock.Clock

/** Moves rolling relay entries through their life cycle and keeps a journal of what it did. */
data class RollingRelayEntryManager(
    var store: RollingRelayEntryStore,
    var validator: RollingRelayEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling relay entry $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRelayEntryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling relay entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRelayEntryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newRollingRelayEntryManager(store: RollingRelayEntryStore, clock: Clock): RollingRelayEntryManager {
    return RollingRelayEntryManager(
        store = store,
        validator = defaultRollingRelayEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
