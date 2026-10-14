package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.safety.clock.Clock

/** Moves camera event summaries through their life cycle and keeps a journal of what it did. */
data class CameraEventSummaryDesk(
    var store: CameraEventSummaryStore,
    var validator: CameraEventSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event summary $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newCameraEventSummaryDesk(store: CameraEventSummaryStore, clock: Clock): CameraEventSummaryDesk {
    return CameraEventSummaryDesk(
        store = store,
        validator = defaultCameraEventSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
