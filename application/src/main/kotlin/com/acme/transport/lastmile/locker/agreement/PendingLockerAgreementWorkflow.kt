package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.lastmile.clock.Clock

/** Moves pending locker agreements through their life cycle and keeps a journal of what it did. */
data class PendingLockerAgreementDesk(
    var store: PendingLockerAgreementStore,
    var validator: PendingLockerAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingLockerAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending locker agreement $code not found")
        validator.requireValid(item)
        item.moveTo(PendingLockerAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending locker agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingLockerAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending locker agreement $code not found")
        val result = engine.run(item)
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

fun newPendingLockerAgreementDesk(store: PendingLockerAgreementStore, clock: Clock): PendingLockerAgreementDesk {
    return PendingLockerAgreementDesk(
        store = store,
        validator = defaultPendingLockerAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingLockerAgreementEngine(),
    )
}
