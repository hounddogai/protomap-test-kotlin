package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.terminal.clock.Clock

/** Moves lenient trailer yard segments through their life cycle and keeps a journal of what it did. */
data class LenientTrailerYardSegmentWorkflow(
    var store: LenientTrailerYardSegmentStore,
    var validator: LenientTrailerYardSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient trailer yard segment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientTrailerYardSegmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient trailer yard segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientTrailerYardSegmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLenientTrailerYardSegmentWorkflow(store: LenientTrailerYardSegmentStore, clock: Clock): LenientTrailerYardSegmentWorkflow {
    return LenientTrailerYardSegmentWorkflow(
        store = store,
        validator = defaultLenientTrailerYardSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
