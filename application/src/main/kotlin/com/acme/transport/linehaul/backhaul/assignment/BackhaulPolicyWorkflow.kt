package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul policies through their life cycle and keeps a journal of what it did. */
data class BackhaulPolicyDesk(
    var store: BackhaulPolicyStore,
    var validator: BackhaulPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("backhaul policy $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulPolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulPolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("backhaul policy $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newBackhaulPolicyDesk(store: BackhaulPolicyStore, clock: Clock): BackhaulPolicyDesk {
    return BackhaulPolicyDesk(
        store = store,
        validator = defaultBackhaulPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulPolicyEngine(),
    )
}
