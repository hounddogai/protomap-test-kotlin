package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves adaptive dwell estimates through their life cycle and keeps a journal of what it did. */
data class AdaptiveDwellEstimateService(
    var store: AdaptiveDwellEstimateStore,
    var validator: AdaptiveDwellEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDwellEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive dwell estimate $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDwellEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive dwell estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDwellEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive dwell estimate $code not found")
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

fun newAdaptiveDwellEstimateService(store: AdaptiveDwellEstimateStore, clock: Clock, flags: Registry): AdaptiveDwellEstimateService {
    return AdaptiveDwellEstimateService(
        store = store,
        validator = defaultAdaptiveDwellEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDwellEstimateEngine(flags),
    )
}
