package com.acme.transport.terminal.dwell.score

import com.acme.transport.terminal.clock.Clock

/** Moves dwell rollups through their life cycle and keeps a journal of what it did. */
data class DwellRollupDesk(
    var store: DwellRollupStore,
    var validator: DwellRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell rollup $code not found")
        validator.requireValid(item)
        item.moveTo(DwellRollupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellRollupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dwell rollup $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDwellRollupDesk(store: DwellRollupStore, clock: Clock): DwellRollupDesk {
    return DwellRollupDesk(
        store = store,
        validator = defaultDwellRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellRollupEngine(),
    )
}
