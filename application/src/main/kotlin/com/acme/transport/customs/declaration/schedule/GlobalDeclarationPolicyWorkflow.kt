package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.clock.Clock

/** Moves global declaration policies through their life cycle and keeps a journal of what it did. */
data class GlobalDeclarationPolicyService(
    var store: GlobalDeclarationPolicyStore,
    var validator: GlobalDeclarationPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global declaration policy $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDeclarationPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global declaration policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDeclarationPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newGlobalDeclarationPolicyService(store: GlobalDeclarationPolicyStore, clock: Clock): GlobalDeclarationPolicyService {
    return GlobalDeclarationPolicyService(
        store = store,
        validator = defaultGlobalDeclarationPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
