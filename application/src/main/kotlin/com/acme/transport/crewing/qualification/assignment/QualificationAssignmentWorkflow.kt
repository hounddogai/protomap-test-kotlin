package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves qualification assignments through their life cycle and keeps a journal of what it did. */
data class QualificationAssignmentManager(
    var store: QualificationAssignmentStore,
    var validator: QualificationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("qualification assignment $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationAssignmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationAssignmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("qualification assignment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newQualificationAssignmentManager(store: QualificationAssignmentStore, clock: Clock, flags: Registry): QualificationAssignmentManager {
    return QualificationAssignmentManager(
        store = store,
        validator = defaultQualificationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationAssignmentEngine(flags),
    )
}
