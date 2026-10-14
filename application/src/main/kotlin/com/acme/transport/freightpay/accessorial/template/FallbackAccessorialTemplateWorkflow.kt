package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves fallback accessorial templates through their life cycle and keeps a journal of what it did.
 */
data class FallbackAccessorialTemplateCoordinator(
    var store: FallbackAccessorialTemplateStore,
    var validator: FallbackAccessorialTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback accessorial template $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackAccessorialTemplateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback accessorial template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackAccessorialTemplateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newFallbackAccessorialTemplateCoordinator(store: FallbackAccessorialTemplateStore, clock: Clock): FallbackAccessorialTemplateCoordinator {
    return FallbackAccessorialTemplateCoordinator(
        store = store,
        validator = defaultFallbackAccessorialTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
