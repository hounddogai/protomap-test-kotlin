package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.linehaul.clock.Clock

/** Moves capped lane plan policies through their life cycle and keeps a journal of what it did. */
data class CappedLanePlanPolicyDesk(
    var store: CappedLanePlanPolicyStore,
    var validator: CappedLanePlanPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped lane plan policy $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLanePlanPolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped lane plan policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLanePlanPolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newCappedLanePlanPolicyDesk(store: CappedLanePlanPolicyStore, clock: Clock): CappedLanePlanPolicyDesk {
    return CappedLanePlanPolicyDesk(
        store = store,
        validator = defaultCappedLanePlanPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
