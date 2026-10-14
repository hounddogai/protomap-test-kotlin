package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.safety.clock.Clock

/** Moves global scorecard allocations through their life cycle and keeps a journal of what it did. */
data class GlobalScorecardAllocationManager(
    var store: GlobalScorecardAllocationStore,
    var validator: GlobalScorecardAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalScorecardAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global scorecard allocation $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalScorecardAllocationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global scorecard allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalScorecardAllocationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global scorecard allocation $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newGlobalScorecardAllocationManager(store: GlobalScorecardAllocationStore, clock: Clock): GlobalScorecardAllocationManager {
    return GlobalScorecardAllocationManager(
        store = store,
        validator = defaultGlobalScorecardAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalScorecardAllocationEngine(),
    )
}
