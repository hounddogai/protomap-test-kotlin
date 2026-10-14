package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dwell quotas through their life cycle and keeps a journal of what it did. */
data class DwellQuotaManager(
    var store: DwellQuotaStore,
    var validator: DwellQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dwell quota $code not found")
        validator.requireValid(item)
        item.moveTo(DwellQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dwell quota $code not found")
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

fun newDwellQuotaManager(store: DwellQuotaStore, clock: Clock, flags: Registry): DwellQuotaManager {
    return DwellQuotaManager(
        store = store,
        validator = defaultDwellQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellQuotaEngine(flags),
    )
}
