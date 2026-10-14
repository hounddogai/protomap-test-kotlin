package com.acme.transport.network.transittime.timedigest

import com.acme.transport.network.clock.Clock

/** Moves lenient transit time audits through their life cycle and keeps a journal of what it did. */
data class LenientTransitTimeAuditCoordinator(
    var store: LenientTransitTimeAuditStore,
    var validator: LenientTransitTimeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient transit time audit $code not found")
        validator.requireValid(item)
        item.moveTo(LenientTransitTimeAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient transit time audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientTransitTimeAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLenientTransitTimeAuditCoordinator(store: LenientTransitTimeAuditStore, clock: Clock): LenientTransitTimeAuditCoordinator {
    return LenientTransitTimeAuditCoordinator(
        store = store,
        validator = defaultLenientTransitTimeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
