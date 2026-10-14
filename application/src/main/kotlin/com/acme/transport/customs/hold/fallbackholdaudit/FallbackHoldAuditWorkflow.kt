package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.clock.Clock

/** Moves fallback hold audits through their life cycle and keeps a journal of what it did. */
data class FallbackHoldAuditManager(
    var store: FallbackHoldAuditStore,
    var validator: FallbackHoldAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback hold audit $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackHoldAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback hold audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackHoldAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newFallbackHoldAuditManager(store: FallbackHoldAuditStore, clock: Clock): FallbackHoldAuditManager {
    return FallbackHoldAuditManager(
        store = store,
        validator = defaultFallbackHoldAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
