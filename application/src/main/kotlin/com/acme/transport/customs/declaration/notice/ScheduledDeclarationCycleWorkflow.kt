package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled declaration cycles through their life cycle and keeps a journal of what it did. */
data class ScheduledDeclarationCycleDesk(
    var store: ScheduledDeclarationCycleStore,
    var validator: ScheduledDeclarationCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDeclarationCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled declaration cycle $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDeclarationCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled declaration cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDeclarationCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled declaration cycle $code not found")
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

fun newScheduledDeclarationCycleDesk(store: ScheduledDeclarationCycleStore, clock: Clock, flags: Registry): ScheduledDeclarationCycleDesk {
    return ScheduledDeclarationCycleDesk(
        store = store,
        validator = defaultScheduledDeclarationCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDeclarationCycleEngine(flags),
    )
}
