package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.safety.clock.Clock

/** Moves global camera event summaries through their life cycle and keeps a journal of what it did. */
data class GlobalCameraEventSummaryService(
    var store: GlobalCameraEventSummaryStore,
    var validator: GlobalCameraEventSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global camera event summary $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCameraEventSummaryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global camera event summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCameraEventSummaryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newGlobalCameraEventSummaryService(store: GlobalCameraEventSummaryStore, clock: Clock): GlobalCameraEventSummaryService {
    return GlobalCameraEventSummaryService(
        store = store,
        validator = defaultGlobalCameraEventSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
