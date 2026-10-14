package com.acme.transport.safety.violation.rollup

import com.acme.transport.safety.clock.Clock

/** Moves violation rollups through their life cycle and keeps a journal of what it did. */
data class ViolationRollupCoordinator(
    var store: ViolationRollupStore,
    var validator: ViolationRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("violation rollup $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationRollupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("violation rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationRollupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("violation rollup $code not found")
        val result = engine.run(item)
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

fun newViolationRollupCoordinator(store: ViolationRollupStore, clock: Clock): ViolationRollupCoordinator {
    return ViolationRollupCoordinator(
        store = store,
        validator = defaultViolationRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationRollupEngine(),
    )
}
