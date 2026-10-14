package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.clock.Clock

/**
 * Moves adaptive declaration variances through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveDeclarationVarianceService(
    var store: AdaptiveDeclarationVarianceStore,
    var validator: AdaptiveDeclarationVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive declaration variance $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDeclarationVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive declaration variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDeclarationVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newAdaptiveDeclarationVarianceService(store: AdaptiveDeclarationVarianceStore, clock: Clock): AdaptiveDeclarationVarianceService {
    return AdaptiveDeclarationVarianceService(
        store = store,
        validator = defaultAdaptiveDeclarationVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
