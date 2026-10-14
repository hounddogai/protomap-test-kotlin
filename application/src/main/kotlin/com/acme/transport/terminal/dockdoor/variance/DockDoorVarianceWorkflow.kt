package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.terminal.clock.Clock

/** Moves dock door variances through their life cycle and keeps a journal of what it did. */
data class DockDoorVarianceService(
    var store: DockDoorVarianceStore,
    var validator: DockDoorVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dock door variance $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door variance $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newDockDoorVarianceService(store: DockDoorVarianceStore, clock: Clock): DockDoorVarianceService {
    return DockDoorVarianceService(
        store = store,
        validator = defaultDockDoorVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorVarianceEngine(),
    )
}
