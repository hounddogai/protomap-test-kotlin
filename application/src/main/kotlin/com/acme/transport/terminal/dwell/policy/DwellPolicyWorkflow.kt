package com.acme.transport.terminal.dwell.policy

import com.acme.transport.terminal.clock.Clock

/** Moves dwell policies through their life cycle and keeps a journal of what it did. */
data class DwellPolicyService(
    var store: DwellPolicyStore,
    var validator: DwellPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell policy $code not found")
        validator.requireValid(item)
        item.moveTo(DwellPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newDwellPolicyService(store: DwellPolicyStore, clock: Clock): DwellPolicyService {
    return DwellPolicyService(
        store = store,
        validator = defaultDwellPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
