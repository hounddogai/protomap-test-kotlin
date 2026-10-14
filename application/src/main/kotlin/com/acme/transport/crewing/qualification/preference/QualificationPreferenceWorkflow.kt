package com.acme.transport.crewing.qualification.preference

import com.acme.transport.crewing.clock.Clock

/** Moves qualification preferences through their life cycle and keeps a journal of what it did. */
data class QualificationPreferenceService(
    var store: QualificationPreferenceStore,
    var validator: QualificationPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("qualification preference $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationPreferenceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationPreferenceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newQualificationPreferenceService(store: QualificationPreferenceStore, clock: Clock): QualificationPreferenceService {
    return QualificationPreferenceService(
        store = store,
        validator = defaultQualificationPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
