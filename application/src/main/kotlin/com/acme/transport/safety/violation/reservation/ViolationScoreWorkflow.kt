package com.acme.transport.safety.violation.reservation

import com.acme.transport.safety.clock.Clock

/** Moves violation scores through their life cycle and keeps a journal of what it did. */
data class ViolationScoreDesk(
    var store: ViolationScoreStore,
    var validator: ViolationScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("violation score $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("violation score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newViolationScoreDesk(store: ViolationScoreStore, clock: Clock): ViolationScoreDesk {
    return ViolationScoreDesk(
        store = store,
        validator = defaultViolationScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
