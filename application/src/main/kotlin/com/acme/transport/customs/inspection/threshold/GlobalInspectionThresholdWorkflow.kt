package com.acme.transport.customs.inspection.threshold

import com.acme.transport.customs.clock.Clock

/** Moves global inspection thresholds through their life cycle and keeps a journal of what it did. */
data class GlobalInspectionThresholdDesk(
    var store: GlobalInspectionThresholdStore,
    var validator: GlobalInspectionThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global inspection threshold $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalInspectionThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global inspection threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalInspectionThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newGlobalInspectionThresholdDesk(store: GlobalInspectionThresholdStore, clock: Clock): GlobalInspectionThresholdDesk {
    return GlobalInspectionThresholdDesk(
        store = store,
        validator = defaultGlobalInspectionThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
