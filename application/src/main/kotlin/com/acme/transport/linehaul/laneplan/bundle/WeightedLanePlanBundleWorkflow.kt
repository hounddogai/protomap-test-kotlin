package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.linehaul.clock.Clock

/** Moves weighted lane plan bundles through their life cycle and keeps a journal of what it did. */
data class WeightedLanePlanBundleWorkflow(
    var store: WeightedLanePlanBundleStore,
    var validator: WeightedLanePlanBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted lane plan bundle $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedLanePlanBundleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted lane plan bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedLanePlanBundleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newWeightedLanePlanBundleWorkflow(store: WeightedLanePlanBundleStore, clock: Clock): WeightedLanePlanBundleWorkflow {
    return WeightedLanePlanBundleWorkflow(
        store = store,
        validator = defaultWeightedLanePlanBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
