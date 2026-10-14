package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.linehaul.clock.Clock

/** Moves rolling capacity cycles through their life cycle and keeps a journal of what it did. */
data class RollingCapacityCycleManager(
    var store: RollingCapacityCycleStore,
    var validator: RollingCapacityCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling capacity cycle $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCapacityCycleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling capacity cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCapacityCycleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newRollingCapacityCycleManager(store: RollingCapacityCycleStore, clock: Clock): RollingCapacityCycleManager {
    return RollingCapacityCycleManager(
        store = store,
        validator = defaultRollingCapacityCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
