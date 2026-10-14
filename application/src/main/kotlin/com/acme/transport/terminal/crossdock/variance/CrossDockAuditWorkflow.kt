package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock audits through their life cycle and keeps a journal of what it did. */
data class CrossDockAuditService(
    var store: CrossDockAuditStore,
    var validator: CrossDockAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cross dock audit $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("cross dock audit $code not found")
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

fun newCrossDockAuditService(store: CrossDockAuditStore, clock: Clock): CrossDockAuditService {
    return CrossDockAuditService(
        store = store,
        validator = defaultCrossDockAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockAuditEngine(),
    )
}
