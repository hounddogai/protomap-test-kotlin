package com.acme.transport.network.node.tier

import com.acme.transport.network.clock.Clock

/** Moves strict node tiers through their life cycle and keeps a journal of what it did. */
data class StrictNodeTierDesk(
    var store: StrictNodeTierStore,
    var validator: StrictNodeTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictNodeTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict node tier $code not found")
        validator.requireValid(item)
        item.moveTo(StrictNodeTierStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict node tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictNodeTierStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict node tier $code not found")
        val result = engine.price(item)
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

fun newStrictNodeTierDesk(store: StrictNodeTierStore, clock: Clock): StrictNodeTierDesk {
    return StrictNodeTierDesk(
        store = store,
        validator = defaultStrictNodeTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictNodeTierEngine(),
    )
}
