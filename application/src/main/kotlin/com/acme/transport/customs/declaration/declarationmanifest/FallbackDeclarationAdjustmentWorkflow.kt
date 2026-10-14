package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.customs.clock.Clock

/**
 * Moves fallback declaration adjustments through their life cycle and keeps a journal of what it did.
 */
data class FallbackDeclarationAdjustmentManager(
    var store: FallbackDeclarationAdjustmentStore,
    var validator: FallbackDeclarationAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback declaration adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDeclarationAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback declaration adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDeclarationAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newFallbackDeclarationAdjustmentManager(store: FallbackDeclarationAdjustmentStore, clock: Clock): FallbackDeclarationAdjustmentManager {
    return FallbackDeclarationAdjustmentManager(
        store = store,
        validator = defaultFallbackDeclarationAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
