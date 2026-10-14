package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default ev charge scores through their life cycle and keeps a journal of what it did. */
data class DefaultEvChargeScoreCoordinator(
    var store: DefaultEvChargeScoreStore,
    var validator: DefaultEvChargeScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultEvChargeScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default ev charge score $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultEvChargeScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default ev charge score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultEvChargeScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default ev charge score $code not found")
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

fun newDefaultEvChargeScoreCoordinator(store: DefaultEvChargeScoreStore, clock: Clock, flags: Registry): DefaultEvChargeScoreCoordinator {
    return DefaultEvChargeScoreCoordinator(
        store = store,
        validator = defaultDefaultEvChargeScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultEvChargeScoreEngine(flags),
    )
}
