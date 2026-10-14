package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.safety.clock.Clock

/** Moves lenient violation quotas through their life cycle and keeps a journal of what it did. */
data class LenientViolationQuotaCoordinator(
    var store: LenientViolationQuotaStore,
    var validator: LenientViolationQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient violation quota $code not found")
        validator.requireValid(item)
        item.moveTo(LenientViolationQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient violation quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientViolationQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLenientViolationQuotaCoordinator(store: LenientViolationQuotaStore, clock: Clock): LenientViolationQuotaCoordinator {
    return LenientViolationQuotaCoordinator(
        store = store,
        validator = defaultLenientViolationQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
