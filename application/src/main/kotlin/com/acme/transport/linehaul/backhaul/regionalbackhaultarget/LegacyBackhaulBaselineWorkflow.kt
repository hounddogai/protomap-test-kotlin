package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.linehaul.clock.Clock

/** Moves legacy backhaul baselines through their life cycle and keeps a journal of what it did. */
data class LegacyBackhaulBaselineDesk(
    var store: LegacyBackhaulBaselineStore,
    var validator: LegacyBackhaulBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyBackhaulBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy backhaul baseline $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyBackhaulBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy backhaul baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyBackhaulBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy backhaul baseline $code not found")
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

fun newLegacyBackhaulBaselineDesk(store: LegacyBackhaulBaselineStore, clock: Clock): LegacyBackhaulBaselineDesk {
    return LegacyBackhaulBaselineDesk(
        store = store,
        validator = defaultLegacyBackhaulBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyBackhaulBaselineEngine(),
    )
}
