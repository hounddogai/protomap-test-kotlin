package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.clock.Clock

/**
 * Moves regional qualification notices through their life cycle and keeps a journal of what it did.
 */
data class RegionalQualificationNoticeDesk(
    var store: RegionalQualificationNoticeStore,
    var validator: RegionalQualificationNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional qualification notice $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalQualificationNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional qualification notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalQualificationNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newRegionalQualificationNoticeDesk(store: RegionalQualificationNoticeStore, clock: Clock): RegionalQualificationNoticeDesk {
    return RegionalQualificationNoticeDesk(
        store = store,
        validator = defaultRegionalQualificationNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
