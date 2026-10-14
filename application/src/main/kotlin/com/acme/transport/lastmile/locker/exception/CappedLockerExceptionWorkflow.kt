package com.acme.transport.lastmile.locker.exception

import com.acme.transport.lastmile.clock.Clock

/** Moves capped locker exceptions through their life cycle and keeps a journal of what it did. */
data class CappedLockerExceptionManager(
    var store: CappedLockerExceptionStore,
    var validator: CappedLockerExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedLockerExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped locker exception $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLockerExceptionStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped locker exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLockerExceptionStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped locker exception $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newCappedLockerExceptionManager(store: CappedLockerExceptionStore, clock: Clock): CappedLockerExceptionManager {
    return CappedLockerExceptionManager(
        store = store,
        validator = defaultCappedLockerExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedLockerExceptionEngine(),
    )
}
