package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.clock.Clock

/** Moves tire notices through their life cycle and keeps a journal of what it did. */
data class TireNoticeService(
    var store: TireNoticeStore,
    var validator: TireNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tire notice $code not found")
        validator.requireValid(item)
        item.moveTo(TireNoticeStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tire notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireNoticeStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newTireNoticeService(store: TireNoticeStore, clock: Clock): TireNoticeService {
    return TireNoticeService(
        store = store,
        validator = defaultTireNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
