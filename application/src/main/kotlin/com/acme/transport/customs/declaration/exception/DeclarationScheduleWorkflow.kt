package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.clock.Clock

/** Moves declaration schedules through their life cycle and keeps a journal of what it did. */
data class DeclarationScheduleCoordinator(
    var store: DeclarationScheduleStore,
    var validator: DeclarationScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("declaration schedule $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("declaration schedule $code not found")
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

fun newDeclarationScheduleCoordinator(store: DeclarationScheduleStore, clock: Clock): DeclarationScheduleCoordinator {
    return DeclarationScheduleCoordinator(
        store = store,
        validator = defaultDeclarationScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationScheduleEngine(),
    )
}
