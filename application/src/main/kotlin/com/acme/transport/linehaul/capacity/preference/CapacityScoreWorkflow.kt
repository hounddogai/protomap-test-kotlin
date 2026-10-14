package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity scores through their life cycle and keeps a journal of what it did. */
data class CapacityScoreDesk(
    var store: CapacityScoreStore,
    var validator: CapacityScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capacity score $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityScoreStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityScoreStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newCapacityScoreDesk(store: CapacityScoreStore, clock: Clock): CapacityScoreDesk {
    return CapacityScoreDesk(
        store = store,
        validator = defaultCapacityScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
