package com.acme.transport.network.servicearea.request

import com.acme.transport.network.clock.Clock

/** Moves fallback service area audits through their life cycle and keeps a journal of what it did. */
data class FallbackServiceAreaAuditDesk(
    var store: FallbackServiceAreaAuditStore,
    var validator: FallbackServiceAreaAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback service area audit $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackServiceAreaAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback service area audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackServiceAreaAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newFallbackServiceAreaAuditDesk(store: FallbackServiceAreaAuditStore, clock: Clock): FallbackServiceAreaAuditDesk {
    return FallbackServiceAreaAuditDesk(
        store = store,
        validator = defaultFallbackServiceAreaAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
