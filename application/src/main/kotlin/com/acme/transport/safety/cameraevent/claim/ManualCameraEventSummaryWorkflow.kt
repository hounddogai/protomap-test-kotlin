package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.safety.clock.Clock

/** Moves manual camera event summaries through their life cycle and keeps a journal of what it did. */
data class ManualCameraEventSummaryWorkflow(
    var store: ManualCameraEventSummaryStore,
    var validator: ManualCameraEventSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualCameraEventSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual camera event summary $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCameraEventSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual camera event summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCameraEventSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual camera event summary $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newManualCameraEventSummaryWorkflow(store: ManualCameraEventSummaryStore, clock: Clock): ManualCameraEventSummaryWorkflow {
    return ManualCameraEventSummaryWorkflow(
        store = store,
        validator = defaultManualCameraEventSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualCameraEventSummaryEngine(),
    )
}
