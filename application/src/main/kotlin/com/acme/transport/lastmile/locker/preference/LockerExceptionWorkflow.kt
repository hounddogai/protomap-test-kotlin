package com.acme.transport.lastmile.locker.preference

import com.acme.transport.lastmile.clock.Clock

/** Moves locker exceptions through their life cycle and keeps a journal of what it did. */
data class LockerExceptionCoordinator(
    var store: LockerExceptionStore,
    var validator: LockerExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("locker exception $code not found")
        validator.requireValid(item)
        item.moveTo(LockerExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("locker exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerExceptionStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("locker exception $code not found")
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

fun newLockerExceptionCoordinator(store: LockerExceptionStore, clock: Clock): LockerExceptionCoordinator {
    return LockerExceptionCoordinator(
        store = store,
        validator = defaultLockerExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerExceptionEngine(),
    )
}
