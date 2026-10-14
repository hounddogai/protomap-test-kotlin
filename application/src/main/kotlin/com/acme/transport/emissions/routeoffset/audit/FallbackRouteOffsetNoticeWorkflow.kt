package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.clock.Clock

/** Moves fallback route offset notices through their life cycle and keeps a journal of what it did. */
data class FallbackRouteOffsetNoticeCoordinator(
    var store: FallbackRouteOffsetNoticeStore,
    var validator: FallbackRouteOffsetNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback route offset notice $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRouteOffsetNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback route offset notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRouteOffsetNoticeStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newFallbackRouteOffsetNoticeCoordinator(store: FallbackRouteOffsetNoticeStore, clock: Clock): FallbackRouteOffsetNoticeCoordinator {
    return FallbackRouteOffsetNoticeCoordinator(
        store = store,
        validator = defaultFallbackRouteOffsetNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
