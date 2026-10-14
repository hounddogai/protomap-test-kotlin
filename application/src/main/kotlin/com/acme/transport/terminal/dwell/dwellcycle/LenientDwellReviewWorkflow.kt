package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.terminal.clock.Clock

/** Moves lenient dwell reviews through their life cycle and keeps a journal of what it did. */
data class LenientDwellReviewWorkflow(
    var store: LenientDwellReviewStore,
    var validator: LenientDwellReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDwellReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient dwell review $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDwellReviewStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dwell review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDwellReviewStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient dwell review $code not found")
        val result = engine.run(item)
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

fun newLenientDwellReviewWorkflow(store: LenientDwellReviewStore, clock: Clock): LenientDwellReviewWorkflow {
    return LenientDwellReviewWorkflow(
        store = store,
        validator = defaultLenientDwellReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDwellReviewEngine(),
    )
}
