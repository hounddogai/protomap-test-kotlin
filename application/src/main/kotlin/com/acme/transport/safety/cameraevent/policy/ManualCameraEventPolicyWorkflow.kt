package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves manual camera event policies through their life cycle and keeps a journal of what it did. */
data class ManualCameraEventPolicyCoordinator(
    var store: ManualCameraEventPolicyStore,
    var validator: ManualCameraEventPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualCameraEventPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual camera event policy $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCameraEventPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual camera event policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCameraEventPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual camera event policy $code not found")
        val result = engine.evaluate(item)
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

fun newManualCameraEventPolicyCoordinator(store: ManualCameraEventPolicyStore, clock: Clock, flags: Registry): ManualCameraEventPolicyCoordinator {
    return ManualCameraEventPolicyCoordinator(
        store = store,
        validator = defaultManualCameraEventPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualCameraEventPolicyEngine(flags),
    )
}
