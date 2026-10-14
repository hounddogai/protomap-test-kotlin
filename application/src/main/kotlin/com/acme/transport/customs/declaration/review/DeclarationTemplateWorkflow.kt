package com.acme.transport.customs.declaration.review

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration templates through their life cycle and keeps a journal of what it did. */
data class DeclarationTemplateWorkflow(
    var store: DeclarationTemplateStore,
    var validator: DeclarationTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("declaration template $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationTemplateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationTemplateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("declaration template $code not found")
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

fun newDeclarationTemplateWorkflow(store: DeclarationTemplateStore, clock: Clock, flags: Registry): DeclarationTemplateWorkflow {
    return DeclarationTemplateWorkflow(
        store = store,
        validator = defaultDeclarationTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationTemplateEngine(flags),
    )
}
