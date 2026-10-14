package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves capped locker policies through their life cycle and keeps a journal of what it did. */
data class CappedLockerPolicyWorkflow(
    var store: CappedLockerPolicyStore,
    var validator: CappedLockerPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedLockerPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped locker policy $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLockerPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped locker policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLockerPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped locker policy $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedLockerPolicyWorkflow(store: CappedLockerPolicyStore, clock: Clock, flags: Registry): CappedLockerPolicyWorkflow {
    return CappedLockerPolicyWorkflow(
        store = store,
        validator = defaultCappedLockerPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedLockerPolicyEngine(flags),
    )
}
