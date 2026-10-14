package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.linehaul.clock.Clock

/** Moves legacy backhaul rules through their life cycle and keeps a journal of what it did. */
data class LegacyBackhaulRuleService(
    var store: LegacyBackhaulRuleStore,
    var validator: LegacyBackhaulRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy backhaul rule $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyBackhaulRuleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy backhaul rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyBackhaulRuleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newLegacyBackhaulRuleService(store: LegacyBackhaulRuleStore, clock: Clock): LegacyBackhaulRuleService {
    return LegacyBackhaulRuleService(
        store = store,
        validator = defaultLegacyBackhaulRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
