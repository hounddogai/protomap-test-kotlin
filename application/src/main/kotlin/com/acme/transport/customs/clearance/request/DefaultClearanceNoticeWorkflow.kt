package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default clearance notices through their life cycle and keeps a journal of what it did. */
data class DefaultClearanceNoticeWorkflow(
    var store: DefaultClearanceNoticeStore,
    var validator: DefaultClearanceNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultClearanceNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default clearance notice $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultClearanceNoticeStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default clearance notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultClearanceNoticeStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default clearance notice $code not found")
        val result = engine.price(item)
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

fun newDefaultClearanceNoticeWorkflow(store: DefaultClearanceNoticeStore, clock: Clock, flags: Registry): DefaultClearanceNoticeWorkflow {
    return DefaultClearanceNoticeWorkflow(
        store = store,
        validator = defaultDefaultClearanceNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultClearanceNoticeEngine(flags),
    )
}
