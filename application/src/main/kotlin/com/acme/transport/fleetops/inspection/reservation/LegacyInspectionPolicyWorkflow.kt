package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy inspection policies through their life cycle and keeps a journal of what it did. */
data class LegacyInspectionPolicyDesk(
    var store: LegacyInspectionPolicyStore,
    var validator: LegacyInspectionPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyInspectionPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy inspection policy $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyInspectionPolicyStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy inspection policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyInspectionPolicyStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy inspection policy $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newLegacyInspectionPolicyDesk(store: LegacyInspectionPolicyStore, clock: Clock): LegacyInspectionPolicyDesk {
    return LegacyInspectionPolicyDesk(
        store = store,
        validator = defaultLegacyInspectionPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyInspectionPolicyEngine(),
    )
}
