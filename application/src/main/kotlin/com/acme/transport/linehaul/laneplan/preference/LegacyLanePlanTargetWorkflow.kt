package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.linehaul.clock.Clock

/** Moves legacy lane plan targets through their life cycle and keeps a journal of what it did. */
data class LegacyLanePlanTargetCoordinator(
    var store: LegacyLanePlanTargetStore,
    var validator: LegacyLanePlanTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyLanePlanTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy lane plan target $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyLanePlanTargetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy lane plan target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyLanePlanTargetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy lane plan target $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLegacyLanePlanTargetCoordinator(store: LegacyLanePlanTargetStore, clock: Clock): LegacyLanePlanTargetCoordinator {
    return LegacyLanePlanTargetCoordinator(
        store = store,
        validator = defaultLegacyLanePlanTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyLanePlanTargetEngine(),
    )
}
