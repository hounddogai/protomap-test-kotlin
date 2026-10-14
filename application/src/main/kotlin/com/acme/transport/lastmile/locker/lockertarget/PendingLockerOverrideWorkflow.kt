package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.lastmile.clock.Clock

/** Moves pending locker overrides through their life cycle and keeps a journal of what it did. */
data class PendingLockerOverrideCoordinator(
    var store: PendingLockerOverrideStore,
    var validator: PendingLockerOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingLockerOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending locker override $code not found")
        validator.requireValid(item)
        item.moveTo(PendingLockerOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending locker override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingLockerOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending locker override $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingLockerOverrideCoordinator(store: PendingLockerOverrideStore, clock: Clock): PendingLockerOverrideCoordinator {
    return PendingLockerOverrideCoordinator(
        store = store,
        validator = defaultPendingLockerOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingLockerOverrideEngine(),
    )
}
