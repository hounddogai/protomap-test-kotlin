package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.clock.Clock

/** Moves qualification milestones through their life cycle and keeps a journal of what it did. */
data class QualificationMilestoneManager(
    var store: QualificationMilestoneStore,
    var validator: QualificationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("qualification milestone $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("qualification milestone $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newQualificationMilestoneManager(store: QualificationMilestoneStore, clock: Clock): QualificationMilestoneManager {
    return QualificationMilestoneManager(
        store = store,
        validator = defaultQualificationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationMilestoneEngine(),
    )
}
