package com.acme.transport.safety.violation.batch

import com.acme.transport.safety.clock.Clock

/** Moves lenient violation cycles through their life cycle and keeps a journal of what it did. */
data class LenientViolationCycleManager(
    var store: LenientViolationCycleStore,
    var validator: LenientViolationCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient violation cycle $code not found")
        validator.requireValid(item)
        item.moveTo(LenientViolationCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient violation cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientViolationCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newLenientViolationCycleManager(store: LenientViolationCycleStore, clock: Clock): LenientViolationCycleManager {
    return LenientViolationCycleManager(
        store = store,
        validator = defaultLenientViolationCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
