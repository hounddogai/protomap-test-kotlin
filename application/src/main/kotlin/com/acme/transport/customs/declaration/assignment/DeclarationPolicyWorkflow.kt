package com.acme.transport.customs.declaration.assignment

import com.acme.transport.customs.clock.Clock

/** Moves declaration policies through their life cycle and keeps a journal of what it did. */
data class DeclarationPolicyWorkflow(
    var store: DeclarationPolicyStore,
    var validator: DeclarationPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("declaration policy $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationPolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationPolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newDeclarationPolicyWorkflow(store: DeclarationPolicyStore, clock: Clock): DeclarationPolicyWorkflow {
    return DeclarationPolicyWorkflow(
        store = store,
        validator = defaultDeclarationPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
