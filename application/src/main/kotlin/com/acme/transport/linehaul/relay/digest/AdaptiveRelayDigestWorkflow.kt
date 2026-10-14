package com.acme.transport.linehaul.relay.digest

import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive relay digests through their life cycle and keeps a journal of what it did. */
data class AdaptiveRelayDigestDesk(
    var store: AdaptiveRelayDigestStore,
    var validator: AdaptiveRelayDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive relay digest $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveRelayDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive relay digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveRelayDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newAdaptiveRelayDigestDesk(store: AdaptiveRelayDigestStore, clock: Clock): AdaptiveRelayDigestDesk {
    return AdaptiveRelayDigestDesk(
        store = store,
        validator = defaultAdaptiveRelayDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
