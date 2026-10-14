package com.acme.transport.safety.violation.allocation

import com.acme.transport.safety.clock.Clock

/** Moves violation preferences through their life cycle and keeps a journal of what it did. */
data class ViolationPreferenceCoordinator(
    var store: ViolationPreferenceStore,
    var validator: ViolationPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("violation preference $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationPreferenceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("violation preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationPreferenceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("violation preference $code not found")
        val result = engine.run(item)
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

fun newViolationPreferenceCoordinator(store: ViolationPreferenceStore, clock: Clock): ViolationPreferenceCoordinator {
    return ViolationPreferenceCoordinator(
        store = store,
        validator = defaultViolationPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationPreferenceEngine(),
    )
}
