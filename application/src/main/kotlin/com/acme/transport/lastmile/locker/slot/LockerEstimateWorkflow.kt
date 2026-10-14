package com.acme.transport.lastmile.locker.slot

import com.acme.transport.lastmile.clock.Clock

/** Moves locker estimates through their life cycle and keeps a journal of what it did. */
data class LockerEstimateDesk(
    var store: LockerEstimateStore,
    var validator: LockerEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("locker estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LockerEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("locker estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newLockerEstimateDesk(store: LockerEstimateStore, clock: Clock): LockerEstimateDesk {
    return LockerEstimateDesk(
        store = store,
        validator = defaultLockerEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
