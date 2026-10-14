package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves qualification requests through their life cycle and keeps a journal of what it did. */
data class QualificationRequestManager(
    var store: QualificationRequestStore,
    var validator: QualificationRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("qualification request $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationRequestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationRequestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("qualification request $code not found")
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

fun newQualificationRequestManager(store: QualificationRequestStore, clock: Clock, flags: Registry): QualificationRequestManager {
    return QualificationRequestManager(
        store = store,
        validator = defaultQualificationRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationRequestEngine(flags),
    )
}
