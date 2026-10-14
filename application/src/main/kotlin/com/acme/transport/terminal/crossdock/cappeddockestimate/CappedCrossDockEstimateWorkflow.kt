package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.terminal.clock.Clock

/** Moves capped cross dock estimates through their life cycle and keeps a journal of what it did. */
data class CappedCrossDockEstimateService(
    var store: CappedCrossDockEstimateStore,
    var validator: CappedCrossDockEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedCrossDockEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped cross dock estimate $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCrossDockEstimateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped cross dock estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCrossDockEstimateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped cross dock estimate $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newCappedCrossDockEstimateService(store: CappedCrossDockEstimateStore, clock: Clock): CappedCrossDockEstimateService {
    return CappedCrossDockEstimateService(
        store = store,
        validator = defaultCappedCrossDockEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedCrossDockEstimateEngine(),
    )
}
