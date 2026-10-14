package com.acme.transport.lastmile.locker.exception

import com.acme.transport.lastmile.clock.Clock

/** Moves lenient locker exceptions through their life cycle and keeps a journal of what it did. */
data class LenientLockerExceptionDesk(
    var store: LenientLockerExceptionStore,
    var validator: LenientLockerExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient locker exception $code not found")
        validator.requireValid(item)
        item.moveTo(LenientLockerExceptionStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient locker exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientLockerExceptionStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientLockerExceptionDesk(store: LenientLockerExceptionStore, clock: Clock): LenientLockerExceptionDesk {
    return LenientLockerExceptionDesk(
        store = store,
        validator = defaultLenientLockerExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
