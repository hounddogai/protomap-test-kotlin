package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute templates through their life cycle and keeps a journal of what it did. */
data class DisputeTemplateManager(
    var store: DisputeTemplateStore,
    var validator: DisputeTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dispute template $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("dispute template $code not found")
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

fun newDisputeTemplateManager(store: DisputeTemplateStore, clock: Clock, flags: Registry): DisputeTemplateManager {
    return DisputeTemplateManager(
        store = store,
        validator = defaultDisputeTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeTemplateEngine(flags),
    )
}
