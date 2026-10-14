package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul segments through their life cycle and keeps a journal of what it did. */
data class BackhaulSegmentService(
    var store: BackhaulSegmentStore,
    var validator: BackhaulSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("backhaul segment $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulSegmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulSegmentStatus.EXPIRED)
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

fun newBackhaulSegmentService(store: BackhaulSegmentStore, clock: Clock): BackhaulSegmentService {
    return BackhaulSegmentService(
        store = store,
        validator = defaultBackhaulSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
