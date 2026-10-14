package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.linehaul.clock.Clock

/** Moves strict trailer policies through their life cycle and keeps a journal of what it did. */
data class StrictTrailerPolicyService(
    var store: StrictTrailerPolicyStore,
    var validator: StrictTrailerPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict trailer policy $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTrailerPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict trailer policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTrailerPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newStrictTrailerPolicyService(store: StrictTrailerPolicyStore, clock: Clock): StrictTrailerPolicyService {
    return StrictTrailerPolicyService(
        store = store,
        validator = defaultStrictTrailerPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
