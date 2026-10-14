package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.customs.clock.Clock

/** Moves legacy inspection requests through their life cycle and keeps a journal of what it did. */
data class LegacyInspectionRequestDesk(
    var store: LegacyInspectionRequestStore,
    var validator: LegacyInspectionRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy inspection request $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyInspectionRequestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy inspection request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyInspectionRequestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newLegacyInspectionRequestDesk(store: LegacyInspectionRequestStore, clock: Clock): LegacyInspectionRequestDesk {
    return LegacyInspectionRequestDesk(
        store = store,
        validator = defaultLegacyInspectionRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
