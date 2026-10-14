package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling roster rollups through their life cycle and keeps a journal of what it did. */
data class RollingRosterRollupWorkflow(
    var store: RollingRosterRollupStore,
    var validator: RollingRosterRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingRosterRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling roster rollup $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRosterRollupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling roster rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRosterRollupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling roster rollup $code not found")
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

fun newRollingRosterRollupWorkflow(store: RollingRosterRollupStore, clock: Clock, flags: Registry): RollingRosterRollupWorkflow {
    return RollingRosterRollupWorkflow(
        store = store,
        validator = defaultRollingRosterRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingRosterRollupEngine(flags),
    )
}
