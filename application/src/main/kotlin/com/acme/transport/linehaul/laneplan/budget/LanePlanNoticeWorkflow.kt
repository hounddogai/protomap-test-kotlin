package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan notices through their life cycle and keeps a journal of what it did. */
data class LanePlanNoticeDesk(
    var store: LanePlanNoticeStore,
    var validator: LanePlanNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lane plan notice $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanNoticeStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanNoticeStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newLanePlanNoticeDesk(store: LanePlanNoticeStore, clock: Clock): LanePlanNoticeDesk {
    return LanePlanNoticeDesk(
        store = store,
        validator = defaultLanePlanNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
