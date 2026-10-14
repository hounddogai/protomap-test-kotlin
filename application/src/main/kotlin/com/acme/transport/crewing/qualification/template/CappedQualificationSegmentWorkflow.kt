package com.acme.transport.crewing.qualification.template

import com.acme.transport.crewing.clock.Clock

/** Moves capped qualification segments through their life cycle and keeps a journal of what it did. */
data class CappedQualificationSegmentService(
    var store: CappedQualificationSegmentStore,
    var validator: CappedQualificationSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped qualification segment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedQualificationSegmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped qualification segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedQualificationSegmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCappedQualificationSegmentService(store: CappedQualificationSegmentStore, clock: Clock): CappedQualificationSegmentService {
    return CappedQualificationSegmentService(
        store = store,
        validator = defaultCappedQualificationSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
