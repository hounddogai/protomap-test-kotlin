package com.acme.transport.network.node.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves seasonal node agreements through their life cycle and keeps a journal of what it did. */
data class SeasonalNodeAgreementManager(
    var store: SeasonalNodeAgreementStore,
    var validator: SeasonalNodeAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalNodeAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal node agreement $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalNodeAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal node agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalNodeAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal node agreement $code not found")
        val result = engine.evaluate(item)
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

fun newSeasonalNodeAgreementManager(store: SeasonalNodeAgreementStore, clock: Clock, flags: Registry): SeasonalNodeAgreementManager {
    return SeasonalNodeAgreementManager(
        store = store,
        validator = defaultSeasonalNodeAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalNodeAgreementEngine(flags),
    )
}
