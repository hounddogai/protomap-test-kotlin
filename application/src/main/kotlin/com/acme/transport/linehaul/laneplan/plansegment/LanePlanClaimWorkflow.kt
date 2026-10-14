package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan claims through their life cycle and keeps a journal of what it did. */
data class LanePlanClaimService(
    var store: LanePlanClaimStore,
    var validator: LanePlanClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan claim $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lane plan claim $code not found")
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

fun newLanePlanClaimService(store: LanePlanClaimStore, clock: Clock, flags: Registry): LanePlanClaimService {
    return LanePlanClaimService(
        store = store,
        validator = defaultLanePlanClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanClaimEngine(flags),
    )
}
