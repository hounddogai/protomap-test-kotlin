package com.acme.transport.safety.violation.exception

import com.acme.transport.safety.clock.Clock

/** Moves violation exceptions through their life cycle and keeps a journal of what it did. */
data class ViolationExceptionManager(
    var store: ViolationExceptionStore,
    var validator: ViolationExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("violation exception $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationExceptionStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("violation exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationExceptionStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newViolationExceptionManager(store: ViolationExceptionStore, clock: Clock): ViolationExceptionManager {
    return ViolationExceptionManager(
        store = store,
        validator = defaultViolationExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
