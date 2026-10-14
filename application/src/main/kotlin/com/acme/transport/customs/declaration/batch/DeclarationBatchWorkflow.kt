package com.acme.transport.customs.declaration.batch

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration batches through their life cycle and keeps a journal of what it did. */
data class DeclarationBatchDesk(
    var store: DeclarationBatchStore,
    var validator: DeclarationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("declaration batch $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationBatchStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationBatchStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("declaration batch $code not found")
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

fun newDeclarationBatchDesk(store: DeclarationBatchStore, clock: Clock, flags: Registry): DeclarationBatchDesk {
    return DeclarationBatchDesk(
        store = store,
        validator = defaultDeclarationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationBatchEngine(flags),
    )
}
