package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped swap records through their life cycle and keeps a journal of what it did. */
data class CappedSwapRecordWorkflow(
    var store: CappedSwapRecordStore,
    var validator: CappedSwapRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedSwapRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped swap record $code not found")
        validator.requireValid(item)
        item.moveTo(CappedSwapRecordStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped swap record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedSwapRecordStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped swap record $code not found")
        val result = engine.evaluate(item)
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

fun newCappedSwapRecordWorkflow(store: CappedSwapRecordStore, clock: Clock, flags: Registry): CappedSwapRecordWorkflow {
    return CappedSwapRecordWorkflow(
        store = store,
        validator = defaultCappedSwapRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedSwapRecordEngine(flags),
    )
}
