package com.acme.transport.emissions.idling.review

import com.acme.transport.emissions.clock.Clock

/** Moves global idling policies through their life cycle and keeps a journal of what it did. */
data class GlobalIdlingPolicyService(
    var store: GlobalIdlingPolicyStore,
    var validator: GlobalIdlingPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalIdlingPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global idling policy $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalIdlingPolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global idling policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalIdlingPolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global idling policy $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newGlobalIdlingPolicyService(store: GlobalIdlingPolicyStore, clock: Clock): GlobalIdlingPolicyService {
    return GlobalIdlingPolicyService(
        store = store,
        validator = defaultGlobalIdlingPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalIdlingPolicyEngine(),
    )
}
