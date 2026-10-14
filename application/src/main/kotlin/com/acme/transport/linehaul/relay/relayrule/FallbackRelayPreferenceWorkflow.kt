package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback relay preferences through their life cycle and keeps a journal of what it did. */
data class FallbackRelayPreferenceDesk(
    var store: FallbackRelayPreferenceStore,
    var validator: FallbackRelayPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback relay preference $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRelayPreferenceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback relay preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRelayPreferenceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newFallbackRelayPreferenceDesk(store: FallbackRelayPreferenceStore, clock: Clock): FallbackRelayPreferenceDesk {
    return FallbackRelayPreferenceDesk(
        store = store,
        validator = defaultFallbackRelayPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
