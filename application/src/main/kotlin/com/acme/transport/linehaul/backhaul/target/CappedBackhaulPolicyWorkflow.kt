package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.linehaul.clock.Clock

/** Moves capped backhaul policies through their life cycle and keeps a journal of what it did. */
data class CappedBackhaulPolicyDesk(
    var store: CappedBackhaulPolicyStore,
    var validator: CappedBackhaulPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedBackhaulPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped backhaul policy $code not found")
        validator.requireValid(item)
        item.moveTo(CappedBackhaulPolicyStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped backhaul policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedBackhaulPolicyStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped backhaul policy $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newCappedBackhaulPolicyDesk(store: CappedBackhaulPolicyStore, clock: Clock): CappedBackhaulPolicyDesk {
    return CappedBackhaulPolicyDesk(
        store = store,
        validator = defaultCappedBackhaulPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedBackhaulPolicyEngine(),
    )
}
