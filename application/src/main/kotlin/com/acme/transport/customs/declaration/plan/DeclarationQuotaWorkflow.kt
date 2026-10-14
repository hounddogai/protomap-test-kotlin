package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.clock.Clock

/** Moves declaration quotas through their life cycle and keeps a journal of what it did. */
data class DeclarationQuotaManager(
    var store: DeclarationQuotaStore,
    var validator: DeclarationQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration quota $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newDeclarationQuotaManager(store: DeclarationQuotaStore, clock: Clock): DeclarationQuotaManager {
    return DeclarationQuotaManager(
        store = store,
        validator = defaultDeclarationQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
