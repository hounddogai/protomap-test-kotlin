package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.terminal.clock.Clock

/** Moves weighted dock door audits through their life cycle and keeps a journal of what it did. */
data class WeightedDockDoorAuditCoordinator(
    var store: WeightedDockDoorAuditStore,
    var validator: WeightedDockDoorAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDockDoorAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted dock door audit $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDockDoorAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted dock door audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDockDoorAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted dock door audit $code not found")
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

fun newWeightedDockDoorAuditCoordinator(store: WeightedDockDoorAuditStore, clock: Clock): WeightedDockDoorAuditCoordinator {
    return WeightedDockDoorAuditCoordinator(
        store = store,
        validator = defaultWeightedDockDoorAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDockDoorAuditEngine(),
    )
}
