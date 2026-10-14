package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive lane plan templates through their life cycle and keeps a journal of what it did. */
data class AdaptiveLanePlanTemplateCoordinator(
    var store: AdaptiveLanePlanTemplateStore,
    var validator: AdaptiveLanePlanTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive lane plan template $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveLanePlanTemplateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive lane plan template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveLanePlanTemplateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newAdaptiveLanePlanTemplateCoordinator(store: AdaptiveLanePlanTemplateStore, clock: Clock): AdaptiveLanePlanTemplateCoordinator {
    return AdaptiveLanePlanTemplateCoordinator(
        store = store,
        validator = defaultAdaptiveLanePlanTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
