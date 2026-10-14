package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves cross dock agreements through their life cycle and keeps a journal of what it did. */
data class CrossDockAgreementCoordinator(
    var store: CrossDockAgreementStore,
    var validator: CrossDockAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("cross dock agreement $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("cross dock agreement $code not found")
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

fun newCrossDockAgreementCoordinator(store: CrossDockAgreementStore, clock: Clock, flags: Registry): CrossDockAgreementCoordinator {
    return CrossDockAgreementCoordinator(
        store = store,
        validator = defaultCrossDockAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockAgreementEngine(flags),
    )
}
