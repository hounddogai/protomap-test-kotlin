package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves fallback fuel surcharge mappings through their life cycle and keeps a journal of what it did.
 */
data class FallbackFuelSurchargeMappingCoordinator(
    var store: FallbackFuelSurchargeMappingStore,
    var validator: FallbackFuelSurchargeMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback fuel surcharge mapping $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackFuelSurchargeMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback fuel surcharge mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackFuelSurchargeMappingStatus.STOPPED)
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

fun newFallbackFuelSurchargeMappingCoordinator(store: FallbackFuelSurchargeMappingStore, clock: Clock): FallbackFuelSurchargeMappingCoordinator {
    return FallbackFuelSurchargeMappingCoordinator(
        store = store,
        validator = defaultFallbackFuelSurchargeMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
