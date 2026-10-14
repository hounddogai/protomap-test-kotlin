package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves pending qualification assignments through their life cycle and keeps a journal of what it did.
 */
data class PendingQualificationAssignmentService(
    var store: PendingQualificationAssignmentStore,
    var validator: PendingQualificationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingQualificationAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending qualification assignment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingQualificationAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending qualification assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingQualificationAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending qualification assignment $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingQualificationAssignmentService(store: PendingQualificationAssignmentStore, clock: Clock, flags: Registry): PendingQualificationAssignmentService {
    return PendingQualificationAssignmentService(
        store = store,
        validator = defaultPendingQualificationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingQualificationAssignmentEngine(flags),
    )
}
