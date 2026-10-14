package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.lastmile.clock.Clock

/** Moves primary redelivery claims through their life cycle and keeps a journal of what it did. */
data class PrimaryRedeliveryClaimCoordinator(
    var store: PrimaryRedeliveryClaimStore,
    var validator: PrimaryRedeliveryClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary redelivery claim $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryRedeliveryClaimStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary redelivery claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryRedeliveryClaimStatus.CLOSED)
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

fun newPrimaryRedeliveryClaimCoordinator(store: PrimaryRedeliveryClaimStore, clock: Clock): PrimaryRedeliveryClaimCoordinator {
    return PrimaryRedeliveryClaimCoordinator(
        store = store,
        validator = defaultPrimaryRedeliveryClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
