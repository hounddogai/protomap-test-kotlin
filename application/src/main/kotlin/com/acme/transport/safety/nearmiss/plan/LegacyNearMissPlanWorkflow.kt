package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.safety.clock.Clock

/** Moves legacy near miss plans through their life cycle and keeps a journal of what it did. */
data class LegacyNearMissPlanDesk(
    var store: LegacyNearMissPlanStore,
    var validator: LegacyNearMissPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyNearMissPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy near miss plan $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyNearMissPlanStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy near miss plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyNearMissPlanStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy near miss plan $code not found")
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

fun newLegacyNearMissPlanDesk(store: LegacyNearMissPlanStore, clock: Clock): LegacyNearMissPlanDesk {
    return LegacyNearMissPlanDesk(
        store = store,
        validator = defaultLegacyNearMissPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyNearMissPlanEngine(),
    )
}
