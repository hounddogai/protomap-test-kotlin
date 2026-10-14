package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.safety.clock.Clock

/** Moves adaptive near miss exceptions through their life cycle and keeps a journal of what it did. */
data class AdaptiveNearMissExceptionCoordinator(
    var store: AdaptiveNearMissExceptionStore,
    var validator: AdaptiveNearMissExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveNearMissExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive near miss exception $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveNearMissExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive near miss exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveNearMissExceptionStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive near miss exception $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newAdaptiveNearMissExceptionCoordinator(store: AdaptiveNearMissExceptionStore, clock: Clock): AdaptiveNearMissExceptionCoordinator {
    return AdaptiveNearMissExceptionCoordinator(
        store = store,
        validator = defaultAdaptiveNearMissExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveNearMissExceptionEngine(),
    )
}
