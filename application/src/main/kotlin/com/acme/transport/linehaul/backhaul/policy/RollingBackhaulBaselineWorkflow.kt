package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.linehaul.clock.Clock

/** Moves rolling backhaul baselines through their life cycle and keeps a journal of what it did. */
data class RollingBackhaulBaselineService(
    var store: RollingBackhaulBaselineStore,
    var validator: RollingBackhaulBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingBackhaulBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling backhaul baseline $code not found")
        validator.requireValid(item)
        item.moveTo(RollingBackhaulBaselineStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling backhaul baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingBackhaulBaselineStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling backhaul baseline $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRollingBackhaulBaselineService(store: RollingBackhaulBaselineStore, clock: Clock): RollingBackhaulBaselineService {
    return RollingBackhaulBaselineService(
        store = store,
        validator = defaultRollingBackhaulBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingBackhaulBaselineEngine(),
    )
}
