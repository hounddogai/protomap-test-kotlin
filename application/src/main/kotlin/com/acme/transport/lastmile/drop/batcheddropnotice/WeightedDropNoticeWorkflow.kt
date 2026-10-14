package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.lastmile.clock.Clock

/** Moves weighted drop notices through their life cycle and keeps a journal of what it did. */
data class WeightedDropNoticeManager(
    var store: WeightedDropNoticeStore,
    var validator: WeightedDropNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDropNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted drop notice $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDropNoticeStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted drop notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDropNoticeStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted drop notice $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newWeightedDropNoticeManager(store: WeightedDropNoticeStore, clock: Clock): WeightedDropNoticeManager {
    return WeightedDropNoticeManager(
        store = store,
        validator = defaultWeightedDropNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDropNoticeEngine(),
    )
}
