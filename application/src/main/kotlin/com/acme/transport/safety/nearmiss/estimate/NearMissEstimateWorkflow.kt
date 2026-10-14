package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.safety.clock.Clock

/** Moves near miss estimates through their life cycle and keeps a journal of what it did. */
data class NearMissEstimateService(
    var store: NearMissEstimateStore,
    var validator: NearMissEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("near miss estimate $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissEstimateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissEstimateStatus.ARCHIVED)
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

fun newNearMissEstimateService(store: NearMissEstimateStore, clock: Clock): NearMissEstimateService {
    return NearMissEstimateService(
        store = store,
        validator = defaultNearMissEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
