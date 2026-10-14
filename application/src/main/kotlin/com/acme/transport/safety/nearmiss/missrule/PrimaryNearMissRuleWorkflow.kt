package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.safety.clock.Clock

/** Moves primary near miss rules through their life cycle and keeps a journal of what it did. */
data class PrimaryNearMissRuleDesk(
    var store: PrimaryNearMissRuleStore,
    var validator: PrimaryNearMissRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary near miss rule $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryNearMissRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary near miss rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryNearMissRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newPrimaryNearMissRuleDesk(store: PrimaryNearMissRuleStore, clock: Clock): PrimaryNearMissRuleDesk {
    return PrimaryNearMissRuleDesk(
        store = store,
        validator = defaultPrimaryNearMissRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
