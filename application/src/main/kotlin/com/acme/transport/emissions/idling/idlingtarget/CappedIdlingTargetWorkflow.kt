package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.emissions.clock.Clock

/** Moves capped idling targets through their life cycle and keeps a journal of what it did. */
data class CappedIdlingTargetCoordinator(
    var store: CappedIdlingTargetStore,
    var validator: CappedIdlingTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped idling target $code not found")
        validator.requireValid(item)
        item.moveTo(CappedIdlingTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped idling target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedIdlingTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newCappedIdlingTargetCoordinator(store: CappedIdlingTargetStore, clock: Clock): CappedIdlingTargetCoordinator {
    return CappedIdlingTargetCoordinator(
        store = store,
        validator = defaultCappedIdlingTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
