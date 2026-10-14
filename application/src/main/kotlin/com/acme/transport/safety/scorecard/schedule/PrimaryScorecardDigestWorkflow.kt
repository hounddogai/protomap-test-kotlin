package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves primary scorecard digests through their life cycle and keeps a journal of what it did. */
data class PrimaryScorecardDigestCoordinator(
    var store: PrimaryScorecardDigestStore,
    var validator: PrimaryScorecardDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryScorecardDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary scorecard digest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryScorecardDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary scorecard digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryScorecardDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary scorecard digest $code not found")
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

fun newPrimaryScorecardDigestCoordinator(store: PrimaryScorecardDigestStore, clock: Clock, flags: Registry): PrimaryScorecardDigestCoordinator {
    return PrimaryScorecardDigestCoordinator(
        store = store,
        validator = defaultPrimaryScorecardDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryScorecardDigestEngine(flags),
    )
}
