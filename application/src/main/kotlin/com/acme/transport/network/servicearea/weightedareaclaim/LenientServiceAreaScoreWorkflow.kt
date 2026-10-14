package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves lenient service area scores through their life cycle and keeps a journal of what it did. */
data class LenientServiceAreaScoreCoordinator(
    var store: LenientServiceAreaScoreStore,
    var validator: LenientServiceAreaScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientServiceAreaScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient service area score $code not found")
        validator.requireValid(item)
        item.moveTo(LenientServiceAreaScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient service area score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientServiceAreaScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient service area score $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newLenientServiceAreaScoreCoordinator(store: LenientServiceAreaScoreStore, clock: Clock, flags: Registry): LenientServiceAreaScoreCoordinator {
    return LenientServiceAreaScoreCoordinator(
        store = store,
        validator = defaultLenientServiceAreaScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientServiceAreaScoreEngine(flags),
    )
}
