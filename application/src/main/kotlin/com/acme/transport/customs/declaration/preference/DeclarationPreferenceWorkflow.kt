package com.acme.transport.customs.declaration.preference

import com.acme.transport.customs.clock.Clock

/** Moves declaration preferences through their life cycle and keeps a journal of what it did. */
data class DeclarationPreferenceWorkflow(
    var store: DeclarationPreferenceStore,
    var validator: DeclarationPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration preference $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationPreferenceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationPreferenceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("declaration preference $code not found")
        val result = engine.run(item)
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

fun newDeclarationPreferenceWorkflow(store: DeclarationPreferenceStore, clock: Clock): DeclarationPreferenceWorkflow {
    return DeclarationPreferenceWorkflow(
        store = store,
        validator = defaultDeclarationPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationPreferenceEngine(),
    )
}
