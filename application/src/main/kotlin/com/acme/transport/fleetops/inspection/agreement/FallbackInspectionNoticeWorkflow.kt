package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.clock.Clock

/** Moves fallback inspection notices through their life cycle and keeps a journal of what it did. */
data class FallbackInspectionNoticeManager(
    var store: FallbackInspectionNoticeStore,
    var validator: FallbackInspectionNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback inspection notice $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackInspectionNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback inspection notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackInspectionNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newFallbackInspectionNoticeManager(store: FallbackInspectionNoticeStore, clock: Clock): FallbackInspectionNoticeManager {
    return FallbackInspectionNoticeManager(
        store = store,
        validator = defaultFallbackInspectionNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
