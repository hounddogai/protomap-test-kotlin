package com.acme.transport.network.hub.estimate

import com.acme.transport.network.clock.Clock

/** Moves rolling hub estimates through their life cycle and keeps a journal of what it did. */
data class RollingHubEstimateManager(
    var store: RollingHubEstimateStore,
    var validator: RollingHubEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling hub estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RollingHubEstimateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling hub estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingHubEstimateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newRollingHubEstimateManager(store: RollingHubEstimateStore, clock: Clock): RollingHubEstimateManager {
    return RollingHubEstimateManager(
        store = store,
        validator = defaultRollingHubEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
