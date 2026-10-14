package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.terminal.clock.Clock

/** Moves capped dock door templates through their life cycle and keeps a journal of what it did. */
data class CappedDockDoorTemplateDesk(
    var store: CappedDockDoorTemplateStore,
    var validator: CappedDockDoorTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDockDoorTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped dock door template $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDockDoorTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dock door template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDockDoorTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped dock door template $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedDockDoorTemplateDesk(store: CappedDockDoorTemplateStore, clock: Clock): CappedDockDoorTemplateDesk {
    return CappedDockDoorTemplateDesk(
        store = store,
        validator = defaultCappedDockDoorTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDockDoorTemplateEngine(),
    )
}
