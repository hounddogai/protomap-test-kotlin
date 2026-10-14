package com.acme.transport.customs.declaration.review

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration checkpoints through their life cycle and keeps a journal of what it did. */
data class DeclarationCheckpointWorkflow(
    var store: DeclarationCheckpointStore,
    var validator: DeclarationCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("declaration checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("declaration checkpoint $code not found")
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

fun newDeclarationCheckpointWorkflow(store: DeclarationCheckpointStore, clock: Clock, flags: Registry): DeclarationCheckpointWorkflow {
    return DeclarationCheckpointWorkflow(
        store = store,
        validator = defaultDeclarationCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationCheckpointEngine(flags),
    )
}
