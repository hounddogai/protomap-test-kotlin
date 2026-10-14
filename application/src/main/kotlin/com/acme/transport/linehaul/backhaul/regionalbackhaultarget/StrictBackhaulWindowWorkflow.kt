package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.linehaul.clock.Clock

/** Moves strict backhaul windows through their life cycle and keeps a journal of what it did. */
data class StrictBackhaulWindowWorkflow(
    var store: StrictBackhaulWindowStore,
    var validator: StrictBackhaulWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictBackhaulWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict backhaul window $code not found")
        validator.requireValid(item)
        item.moveTo(StrictBackhaulWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict backhaul window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictBackhaulWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict backhaul window $code not found")
        val result = engine.price(item)
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

fun newStrictBackhaulWindowWorkflow(store: StrictBackhaulWindowStore, clock: Clock): StrictBackhaulWindowWorkflow {
    return StrictBackhaulWindowWorkflow(
        store = store,
        validator = defaultStrictBackhaulWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictBackhaulWindowEngine(),
    )
}
