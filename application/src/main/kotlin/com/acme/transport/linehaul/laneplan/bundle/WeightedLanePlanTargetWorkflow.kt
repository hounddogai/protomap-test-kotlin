package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.linehaul.clock.Clock

/** Moves weighted lane plan targets through their life cycle and keeps a journal of what it did. */
data class WeightedLanePlanTargetService(
    var store: WeightedLanePlanTargetStore,
    var validator: WeightedLanePlanTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted lane plan target $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedLanePlanTargetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted lane plan target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedLanePlanTargetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newWeightedLanePlanTargetService(store: WeightedLanePlanTargetStore, clock: Clock): WeightedLanePlanTargetService {
    return WeightedLanePlanTargetService(
        store = store,
        validator = defaultWeightedLanePlanTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
