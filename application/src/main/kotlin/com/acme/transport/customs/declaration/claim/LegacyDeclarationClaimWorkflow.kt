package com.acme.transport.customs.declaration.claim

import com.acme.transport.customs.clock.Clock

/** Moves legacy declaration claims through their life cycle and keeps a journal of what it did. */
data class LegacyDeclarationClaimManager(
    var store: LegacyDeclarationClaimStore,
    var validator: LegacyDeclarationClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy declaration claim $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDeclarationClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy declaration claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDeclarationClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLegacyDeclarationClaimManager(store: LegacyDeclarationClaimStore, clock: Clock): LegacyDeclarationClaimManager {
    return LegacyDeclarationClaimManager(
        store = store,
        validator = defaultLegacyDeclarationClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
