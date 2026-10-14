package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration slots through their life cycle and keeps a journal of what it did. */
data class DeclarationSlotManager(
    var store: DeclarationSlotStore,
    var validator: DeclarationSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration slot $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("declaration slot $code not found")
        val result = engine.compute(item)
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

fun newDeclarationSlotManager(store: DeclarationSlotStore, clock: Clock, flags: Registry): DeclarationSlotManager {
    return DeclarationSlotManager(
        store = store,
        validator = defaultDeclarationSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationSlotEngine(flags),
    )
}
