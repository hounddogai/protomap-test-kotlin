package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.clock.Clock

/** Moves qualification variances through their life cycle and keeps a journal of what it did. */
data class QualificationVarianceDesk(
    var store: QualificationVarianceStore,
    var validator: QualificationVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("qualification variance $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationVarianceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationVarianceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newQualificationVarianceDesk(store: QualificationVarianceStore, clock: Clock): QualificationVarianceDesk {
    return QualificationVarianceDesk(
        store = store,
        validator = defaultQualificationVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
