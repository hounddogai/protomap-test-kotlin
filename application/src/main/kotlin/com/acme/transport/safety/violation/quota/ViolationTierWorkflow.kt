package com.acme.transport.safety.violation.quota

import com.acme.transport.safety.clock.Clock

/** Moves violation tiers through their life cycle and keeps a journal of what it did. */
data class ViolationTierDesk(
    var store: ViolationTierStore,
    var validator: ViolationTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("violation tier $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("violation tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newViolationTierDesk(store: ViolationTierStore, clock: Clock): ViolationTierDesk {
    return ViolationTierDesk(
        store = store,
        validator = defaultViolationTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
