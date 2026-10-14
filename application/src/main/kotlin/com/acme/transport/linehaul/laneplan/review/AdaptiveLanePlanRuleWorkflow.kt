package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive lane plan rules through their life cycle and keeps a journal of what it did. */
data class AdaptiveLanePlanRuleCoordinator(
    var store: AdaptiveLanePlanRuleStore,
    var validator: AdaptiveLanePlanRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive lane plan rule $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveLanePlanRuleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive lane plan rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveLanePlanRuleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newAdaptiveLanePlanRuleCoordinator(store: AdaptiveLanePlanRuleStore, clock: Clock): AdaptiveLanePlanRuleCoordinator {
    return AdaptiveLanePlanRuleCoordinator(
        store = store,
        validator = defaultAdaptiveLanePlanRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
