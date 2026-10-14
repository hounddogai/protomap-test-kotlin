package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge exceptions through their life cycle and keeps a journal of what it did. */
data class EvChargeExceptionCoordinator(
    var store: EvChargeExceptionStore,
    var validator: EvChargeExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("ev charge exception $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeExceptionStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeExceptionStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newEvChargeExceptionCoordinator(store: EvChargeExceptionStore, clock: Clock): EvChargeExceptionCoordinator {
    return EvChargeExceptionCoordinator(
        store = store,
        validator = defaultEvChargeExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
