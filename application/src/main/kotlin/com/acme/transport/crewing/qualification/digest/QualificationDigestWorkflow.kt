package com.acme.transport.crewing.qualification.digest

import com.acme.transport.crewing.clock.Clock

/** Moves qualification digests through their life cycle and keeps a journal of what it did. */
data class QualificationDigestDesk(
    var store: QualificationDigestStore,
    var validator: QualificationDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("qualification digest $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newQualificationDigestDesk(store: QualificationDigestStore, clock: Clock): QualificationDigestDesk {
    return QualificationDigestDesk(
        store = store,
        validator = defaultQualificationDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
