package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves rolling redelivery templates through their life cycle and keeps a journal of what it did. */
data class RollingRedeliveryTemplateManager(
    var store: RollingRedeliveryTemplateStore,
    var validator: RollingRedeliveryTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingRedeliveryTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling redelivery template $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRedeliveryTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling redelivery template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRedeliveryTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling redelivery template $code not found")
        val result = engine.compute(item)
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

fun newRollingRedeliveryTemplateManager(store: RollingRedeliveryTemplateStore, clock: Clock, flags: Registry): RollingRedeliveryTemplateManager {
    return RollingRedeliveryTemplateManager(
        store = store,
        validator = defaultRollingRedeliveryTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingRedeliveryTemplateEngine(flags),
    )
}
