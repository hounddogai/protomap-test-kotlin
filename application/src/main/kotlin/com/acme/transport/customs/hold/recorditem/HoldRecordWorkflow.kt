package com.acme.transport.customs.hold.recorditem

import com.acme.transport.customs.clock.Clock

/** Moves hold records through their life cycle and keeps a journal of what it did. */
data class HoldRecordWorkflow(
    var store: HoldRecordStore,
    var validator: HoldRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hold record $code not found")
        validator.requireValid(item)
        item.moveTo(HoldRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hold record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newHoldRecordWorkflow(store: HoldRecordStore, clock: Clock): HoldRecordWorkflow {
    return HoldRecordWorkflow(
        store = store,
        validator = defaultHoldRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
