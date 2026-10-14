package com.acme.transport.linehaul.relay.claim

import com.acme.transport.linehaul.clock.Clock

/** Moves tiered relay assignments through their life cycle and keeps a journal of what it did. */
data class TieredRelayAssignmentService(
    var store: TieredRelayAssignmentStore,
    var validator: TieredRelayAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered relay assignment $code not found")
        validator.requireValid(item)
        item.moveTo(TieredRelayAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered relay assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredRelayAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newTieredRelayAssignmentService(store: TieredRelayAssignmentStore, clock: Clock): TieredRelayAssignmentService {
    return TieredRelayAssignmentService(
        store = store,
        validator = defaultTieredRelayAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
