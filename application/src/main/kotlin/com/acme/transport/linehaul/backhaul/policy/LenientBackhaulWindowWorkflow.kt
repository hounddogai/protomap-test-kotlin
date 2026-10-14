package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient backhaul windows through their life cycle and keeps a journal of what it did. */
data class LenientBackhaulWindowWorkflow(
    var store: LenientBackhaulWindowStore,
    var validator: LenientBackhaulWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientBackhaulWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient backhaul window $code not found")
        validator.requireValid(item)
        item.moveTo(LenientBackhaulWindowStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient backhaul window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientBackhaulWindowStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient backhaul window $code not found")
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

fun newLenientBackhaulWindowWorkflow(store: LenientBackhaulWindowStore, clock: Clock): LenientBackhaulWindowWorkflow {
    return LenientBackhaulWindowWorkflow(
        store = store,
        validator = defaultLenientBackhaulWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientBackhaulWindowEngine(),
    )
}
