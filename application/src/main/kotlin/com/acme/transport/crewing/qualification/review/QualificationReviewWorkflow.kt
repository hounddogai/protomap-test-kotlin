package com.acme.transport.crewing.qualification.review

import com.acme.transport.crewing.clock.Clock

/** Moves qualification reviews through their life cycle and keeps a journal of what it did. */
data class QualificationReviewManager(
    var store: QualificationReviewStore,
    var validator: QualificationReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("qualification review $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("qualification review $code not found")
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

fun newQualificationReviewManager(store: QualificationReviewStore, clock: Clock): QualificationReviewManager {
    return QualificationReviewManager(
        store = store,
        validator = defaultQualificationReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationReviewEngine(),
    )
}
