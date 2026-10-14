package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.clock.Clock

/** Moves qualification windows through their life cycle and keeps a journal of what it did. */
data class QualificationWindowService(
    var store: QualificationWindowStore,
    var validator: QualificationWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("qualification window $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("qualification window $code not found")
        val result = engine.run(item)
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

fun newQualificationWindowService(store: QualificationWindowStore, clock: Clock): QualificationWindowService {
    return QualificationWindowService(
        store = store,
        validator = defaultQualificationWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationWindowEngine(),
    )
}
