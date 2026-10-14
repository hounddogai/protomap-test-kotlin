package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves primary scorecard targets through their life cycle and keeps a journal of what it did. */
data class PrimaryScorecardTargetDesk(
    var store: PrimaryScorecardTargetStore,
    var validator: PrimaryScorecardTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryScorecardTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary scorecard target $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryScorecardTargetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary scorecard target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryScorecardTargetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary scorecard target $code not found")
        val result = engine.run(item)
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

fun newPrimaryScorecardTargetDesk(store: PrimaryScorecardTargetStore, clock: Clock, flags: Registry): PrimaryScorecardTargetDesk {
    return PrimaryScorecardTargetDesk(
        store = store,
        validator = defaultPrimaryScorecardTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryScorecardTargetEngine(flags),
    )
}
