package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves rolling dwell templates through their life cycle and keeps a journal of what it did. */
data class RollingDwellTemplateCoordinator(
    var store: RollingDwellTemplateStore,
    var validator: RollingDwellTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDwellTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling dwell template $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDwellTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dwell template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDwellTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling dwell template $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newRollingDwellTemplateCoordinator(store: RollingDwellTemplateStore, clock: Clock, flags: Registry): RollingDwellTemplateCoordinator {
    return RollingDwellTemplateCoordinator(
        store = store,
        validator = defaultRollingDwellTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDwellTemplateEngine(flags),
    )
}
