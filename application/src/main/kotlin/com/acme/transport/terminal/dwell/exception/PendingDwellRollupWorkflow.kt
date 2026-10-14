package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves pending dwell rollups through their life cycle and keeps a journal of what it did. */
data class PendingDwellRollupManager(
    var store: PendingDwellRollupStore,
    var validator: PendingDwellRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingDwellRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending dwell rollup $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDwellRollupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending dwell rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDwellRollupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("pending dwell rollup $code not found")
        val result = engine.compute(item)
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

fun newPendingDwellRollupManager(store: PendingDwellRollupStore, clock: Clock, flags: Registry): PendingDwellRollupManager {
    return PendingDwellRollupManager(
        store = store,
        validator = defaultPendingDwellRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingDwellRollupEngine(flags),
    )
}
