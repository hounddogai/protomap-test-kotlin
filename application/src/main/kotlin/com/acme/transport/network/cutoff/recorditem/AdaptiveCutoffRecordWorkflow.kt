package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.network.clock.Clock

/** Moves adaptive cutoff records through their life cycle and keeps a journal of what it did. */
data class AdaptiveCutoffRecordService(
    var store: AdaptiveCutoffRecordStore,
    var validator: AdaptiveCutoffRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveCutoffRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive cutoff record $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveCutoffRecordStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive cutoff record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveCutoffRecordStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive cutoff record $code not found")
        val result = engine.price(item)
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

fun newAdaptiveCutoffRecordService(store: AdaptiveCutoffRecordStore, clock: Clock): AdaptiveCutoffRecordService {
    return AdaptiveCutoffRecordService(
        store = store,
        validator = defaultAdaptiveCutoffRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveCutoffRecordEngine(),
    )
}
